'  **********************************************************************
' 
'  Copyright (c) 2003-2004 ZeroC, Inc. All rights reserved.
' 
'  This copy of Ice is licensed to you under the terms described in the
'  ICE_LICENSE file included in this distribution.
' 
'  **********************************************************************

Imports Demo

Class NestedServer
    Inherits Ice.Application

    Public Overloads Overrides Function run(ByVal args() As String) As Integer
	Dim adapter As Ice.ObjectAdapter = communicator().createObjectAdapter("Nested.Server")
	Dim self As NestedPrx = NestedPrxHelper.uncheckedCast(adapter.createProxy(Ice.Util.stringToIdentity("nestedServer")))
	adapter.add(New NestedI(self), Ice.Util.stringToIdentity("nestedServer"))
	adapter.activate()
	communicator().waitForShutdown()
	Return 0
    End Function

End Class
