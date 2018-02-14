// **********************************************************************
//
// Copyright (c) 2003-2018 ZeroC, Inc. All rights reserved.
//
// This copy of Ice is licensed to you under the terms described in the
// ICE_LICENSE file included in this distribution.
//
// **********************************************************************

package test.Ice.operations;

import java.io.PrintWriter;

import test.Ice.operations.Test.MyClassPrx;
import test.Ice.operations.Test.MyClassPrxHelper;

class BatchOneways
{
    private static void
    test(boolean b)
    {
        if(!b)
        {
            throw new RuntimeException();
        }
    }

    static class BatchRequestInterceptorI implements Ice.BatchRequestInterceptor
    {
        public void
        enqueue(Ice.BatchRequest request, int count, int size)
        {
            test(request.getOperation().equals("opByteSOneway") || request.getOperation().equals("ice_ping"));
            test(request.getProxy().ice_isBatchOneway());

            if(count > 0)
            {
                test(_lastRequestSize + _size == size);
            }
            _count = count;
            _size = size;

            if(_size + request.getSize() > 25000)
            {
                request.getProxy().begin_ice_flushBatchRequests();
                _size = 18; // header
            }

            if(_enabled)
            {
                _lastRequestSize = request.getSize();
                ++_count;
                request.enqueue();
            }
        }

        public void
        setEnqueue(boolean enabled)
        {
            _enabled = enabled;
        }

        public int
        count()
        {
            return _count;
        }

        private boolean _enabled;
        private int _count;
        private int _size;
        private int _lastRequestSize;
    };

    static void
    batchOneways(test.Util.Application app, MyClassPrx p, PrintWriter out)
    {
        final byte[] bs1 = new byte[10  * 1024];

        MyClassPrx batch = MyClassPrxHelper.uncheckedCast(p.ice_batchOneway());
        batch.ice_flushBatchRequests(); // Empty flush

        p.opByteSOnewayCallCount(); // Reset the call count

        for(int i = 0 ; i < 30 ; ++i)
        {
            try
            {
                batch.opByteSOneway(bs1);
            }
            catch(Ice.MemoryLimitException ex)
            {
                test(false);
            }
        }

        int count = 0;
        while(count < 27) // 3 * 9 requests auto-flushed.
        {
            count += p.opByteSOnewayCallCount();
            try
            {
                Thread.sleep(10);
            }
            catch(InterruptedException ex)
            {
            }
        }

        if(batch.ice_getConnection() != null)
        {
            MyClassPrx batch1 = (MyClassPrx)p.ice_batchOneway();
            MyClassPrx batch2 = (MyClassPrx)p.ice_batchOneway();

            batch1.ice_ping();
            batch2.ice_ping();
            batch1.ice_flushBatchRequests();
            batch1.ice_getConnection().close(false);
            batch1.ice_ping();
            batch2.ice_ping();

            batch1.ice_getConnection();
            batch2.ice_getConnection();

            batch1.ice_ping();
            batch1.ice_getConnection().close(false);
            batch1.ice_ping();
            batch2.ice_ping();
        }

        Ice.Identity identity = new Ice.Identity();
        identity.name = "invalid";
        Ice.ObjectPrx batch3 = batch.ice_identity(identity);
        batch3.ice_ping();
        batch3.ice_flushBatchRequests();

        // Make sure that a bogus batch request doesn't cause troubles to other ones.
        batch3.ice_ping();
        batch.ice_ping();
        batch.ice_flushBatchRequests();
        batch.ice_ping();

        if(batch.ice_getConnection() != null)
        {
            Ice.InitializationData initData = app.createInitializationData();
            initData.properties = p.ice_getCommunicator().getProperties()._clone();
            BatchRequestInterceptorI interceptor = new BatchRequestInterceptorI();
            initData.batchRequestInterceptor = interceptor;
            Ice.Communicator ic = app.initialize(initData);

            batch = MyClassPrxHelper.uncheckedCast(ic.stringToProxy(p.toString()).ice_batchOneway());

            test(interceptor.count() == 0);
            batch.ice_ping();
            batch.ice_ping();
            batch.ice_ping();
            test(interceptor.count() == 0);

            interceptor.setEnqueue(true);
            batch.ice_ping();
            batch.ice_ping();
            batch.ice_ping();
            test(interceptor.count() == 3);

            batch.ice_flushBatchRequests();
            batch.ice_ping();
            test(interceptor.count() == 1);

            batch.opByteSOneway(bs1);
            test(interceptor.count() == 2);
            batch.opByteSOneway(bs1);
            test(interceptor.count() == 3);

            batch.opByteSOneway(bs1); // This should trigger the flush
            batch.ice_ping();
            test(interceptor.count() == 2);

            ic.destroy();
        }
    }
}
