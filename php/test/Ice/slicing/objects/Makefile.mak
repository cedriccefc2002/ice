# **********************************************************************
#
# Copyright (c) 2003-2018 ZeroC, Inc. All rights reserved.
#
# This copy of Ice is licensed to you under the terms described in the
# ICE_LICENSE file included in this distribution.
#
# **********************************************************************

top_srcdir	= ..\..\..\..

SLICE_SRCS	= ClientPrivate.ice Forward.ice Test.ice

SLICE2PHPFLAGS 	= -I.

!include $(top_srcdir)\config\Make.rules.mak.php
