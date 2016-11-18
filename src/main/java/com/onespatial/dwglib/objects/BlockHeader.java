package com.onespatial.dwglib.objects;

import java.util.ArrayList;
import java.util.List;

import com.onespatial.dwglib.FileVersion;
import com.onespatial.dwglib.bitstreams.BitBuffer;
import com.onespatial.dwglib.bitstreams.Handle;
import com.onespatial.dwglib.bitstreams.Point3D;

/**
 * parent is BLOCK HEADER
 * 
 * @author Nigel Westbury
 *
 */
public class BlockHeader extends NonEntityObject {

    public String entryName;
    public boolean sixtyFourFlag;
    private boolean blockIsXref;
    private boolean xrefOverlaid;
    public boolean loadedBit;
    public Point3D basePoint;
    public String xrefPName;
    public String blockDescription;
    private int [] previewData;
    public int insertUnits;
    public boolean explodable;
    public int blockScaling;
    public Handle firstEntityHandle;
    public Handle lastEntityHandle;
    public List<Handle> ownedObjectHandles;
    public Handle endBlockHandle;
    public List<List<Handle>> insertHandles;
    public Handle layoutHandle;

    public BlockHeader(ObjectMap objectMap) {
        super(objectMap);
    }
    
    @Override
    public void readObjectTypeSpecificData(BitBuffer dataStream, BitBuffer stringStream, BitBuffer handleStream, FileVersion fileVersion) {
        // 19.4.50 BLOCK HEADER (49) page 155

        entryName = stringStream.getTU();
        sixtyFourFlag = dataStream.getB();
        /*
         * At this point we always seem to be three bits prior to a word
         * that best matches the expected flags.  We get there reliably if
         * we don't read the xrefindex value.  TODO investigate this as this
         * code may not be correct.
         */
//        int xrefordinal = dataStream.getBS();
        boolean xdep = dataStream.getB();
        boolean anonymous = dataStream.getB();
        boolean hasAttributes = dataStream.getB();
        blockIsXref = dataStream.getB();
        xrefOverlaid = dataStream.getB();
        loadedBit = dataStream.getB();
        int ownedObjectCount = dataStream.getBL();
        basePoint = dataStream.get3BD();
        xrefPName = stringStream.getTU();
        
        List<Integer> insertCounts = new ArrayList<>();
        int thisInsertCount = dataStream.getRC();
        while (thisInsertCount != 0) {
            insertCounts.add(thisInsertCount);
            thisInsertCount = dataStream.getRC();
        }
        
        blockDescription = stringStream.getTU();
        int sizeOfPreviewData = dataStream.getBL();
        previewData = dataStream.getBytes(sizeOfPreviewData);
        insertUnits = dataStream.getBS();
        explodable = dataStream.getB();
        blockScaling = dataStream.getRC();

        // The handles
        
        if (!blockIsXref && !xrefOverlaid) {
            firstEntityHandle = handleStream.getHandle(handleOfThisObject);
            lastEntityHandle = handleStream.getHandle(handleOfThisObject);
        }

        ownedObjectHandles = new ArrayList<>();
        for (int i = 0; i< ownedObjectCount; i++) {
            Handle ownedObjectHandle = handleStream.getHandle();
            ownedObjectHandles.add(ownedObjectHandle);
        }

        endBlockHandle = handleStream.getHandle();

        insertHandles = new ArrayList<>();
        for (Integer insertCount : insertCounts) {
            List<Handle> thisList = new ArrayList<>();
            for (int i = 0; i< insertCount; i++) {
                Handle insertHandle = handleStream.getHandle();
                thisList.add(insertHandle);
            }
            insertHandles.add(thisList);
        }

        layoutHandle = handleStream.getHandle();

        handleStream.advanceToByteBoundary();

        dataStream.assertEndOfStream();
        stringStream.assertEndOfStream();
        handleStream.assertEndOfStream();
    }

	public String toString() {
		return "BLOCK HEADER";
	}

}