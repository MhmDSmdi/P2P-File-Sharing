import java.util.ArrayList;
import java.util.Vector;

public class PacketHandler {

    public static final int CHUNK_SIZE = 60000;
    public static final int MAX_INDEX_HEADER = 1;
    public static final int MAX_FILE_SIZE = MAX_INDEX_HEADER * 256 * CHUNK_SIZE;


    public static Vector<byte[]> segmentFile(byte[] data){
        if (data.length < CHUNK_SIZE - MAX_INDEX_HEADER - 1) {
//            System.out.println("length of data: " + data.length);
            Vector<byte[]> chunks = new Vector<>(1);
            chunks.add(new byte[CHUNK_SIZE]);
            chunks.get(0)[0] = 0;
            chunks.get(0)[1] = 1;
            int index = 0;
            for (int i = MAX_INDEX_HEADER + 1; i < data.length; i++) {
                chunks.get(0)[i] = data[index];
                index ++;
            }
            return chunks;
        } else {
            int dataSize = data.length;
//            System.out.println("length of data: " + data.length);
            int numChunk = (int) Math.ceil((dataSize) / (CHUNK_SIZE - MAX_INDEX_HEADER - 1)) + 1;
//            System.out.println("number of chunks: " + numChunk);
            Vector<byte[]> chunks = new Vector<>(numChunk);
            for (int i = 0; i < numChunk; i++) {
                chunks.add(new byte[CHUNK_SIZE]);
                chunks.get(i)[0] = (byte)i;
                if(i != numChunk - 1)
                    chunks.get(i)[1] = 0;
                else
                    chunks.get(i)[1] = 1;

                int chunkPointer = MAX_INDEX_HEADER + 1;
                if ((i * (CHUNK_SIZE - MAX_INDEX_HEADER - 1)) + CHUNK_SIZE - MAX_INDEX_HEADER - 1 > dataSize) {
                    for (int j = i * (CHUNK_SIZE - MAX_INDEX_HEADER - 1); j < dataSize; j++) {
                        chunks.get(i)[chunkPointer] = data[j];
                        chunkPointer++;
                    }
                } else {
                    for (int j = i * (CHUNK_SIZE-MAX_INDEX_HEADER-1) ; j < (i * (CHUNK_SIZE-MAX_INDEX_HEADER-1)) + CHUNK_SIZE-MAX_INDEX_HEADER-1 ; j++) {
                        chunks.get(i)[chunkPointer] = data[j];
                        chunkPointer++;
                    }
                }
                ///TODO
                // if for size less than CHUNK_SIZE
            }
            return chunks;

        }

    }


    public static byte[] reassembleFile(Vector<byte[]> data){
        byte bytes[] = new byte[(data.size()) * (CHUNK_SIZE - MAX_INDEX_HEADER - 1)];
        int index = 0;
        for (int i = 0; i < data.size(); i++){
            for (int j =2; j < data.get(i).length; j++){
                bytes[index] = data.get(i)[j];
                index ++;
            }
        }
        return bytes;
    }
}
