// **********************************************************************
//
// Copyright (c) 2003-2018 ZeroC, Inc. All rights reserved.
//
// This copy of Ice is licensed to you under the terms described in the
// ICE_LICENSE file included in this distribution.
//
// **********************************************************************

package test.Ice.stream.Serialize;

public class Small implements java.io.Serializable // Fewer than 254 bytes when serialized.
{
    public int i;
}
