package com.fasterxml.util.membuf.impl;

import com.fasterxml.util.membuf.MemBuffers;
import com.fasterxml.util.membuf.MembufTestBase;
import com.fasterxml.util.membuf.SegmentAllocator;
import com.fasterxml.util.membuf.impl.MemBufferImpl;

public class BufferCloseTest extends MembufTestBase
{
    public void testClosing() throws Exception
    {
        MemBuffers bufs = new MemBuffers(20, 4, 10);
        MemBufferImpl buffer = (MemBufferImpl)bufs.createBuffer(2, 3);
        SegmentAllocator alloc = bufs.getAllocator();

        // min size 2, so will allocate 2 right away
        assertEquals(2, alloc.getBufferOwnedSegmentCount());
        assertEquals(0, alloc.getReusableSegmentCount());
        
        buffer.appendEntry(new byte[24]); // fills 1 buffer, part of second
        buffer.appendEntry(new byte[17]); // fills 2nd buffer, starts third

        assertEquals(3, alloc.getBufferOwnedSegmentCount());
        assertEquals(0, alloc.getReusableSegmentCount());
        assertEquals(3, buffer._usedSegmentsCount);

        buffer.close();
        
        assertEquals(0, buffer._entryCount);
        assertEquals(0L, buffer._totalPayloadLength);
        assertEquals(0, buffer._usedSegmentsCount);
        assertEquals(0, buffer._freeSegmentCount);

        assertEquals(0, alloc.getBufferOwnedSegmentCount());
        assertEquals(3, alloc.getReusableSegmentCount());

        // Ok: reuse seems to work; but we should NOT be able to do anything else
        try {
            buffer.appendEntry(new byte[1]);
            fail("Should not be able to append things to closed Buffer");
        } catch (IllegalStateException e) {
            verifyException(e, "instance closed");
        }

        try {
            buffer.getNextEntryLength();
            fail("Should not be able to read from closed Buffer");
        } catch (IllegalStateException e) {
            verifyException(e, "instance closed");
        }
    }
}
