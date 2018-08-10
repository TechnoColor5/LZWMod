/**
 * @author Daniel Mailloux
 * @version v1.0
 *
 * This is a modification of the LZW class written by Robert Sedgewick.
 * The modifications made allow for a better overall compression
 * due to a dynamic library size.
 */

/*************************************************************************
 *  Compilation:  javac LZWMod.java
 *  Execution:    java LZWMod - < input.txt   (compress)
 *  Execution:    java LZWMod + < input.txt   (expand)
 *  Dependencies: BinaryIn.java BinaryOut.java
 *
 *  Compress or expand binary input from standard input using LZW.
 *
 *
 *************************************************************************/
import java.util.ArrayList;

public class LZWmod {
    private static final int R = 256;        // number of input chars
    private static int L = 512;       // number of codewords = 2^W
    private static int W = 9;         // codeword width
    private static int MAX;
    private static boolean reset;

    public static void compress() { 
        StringBuilder input;
        TSTmod<Integer> st = new TSTmod<Integer>();          // <----dictionary
        StringBuilder next = new StringBuilder();
        //Add ASCII character set to dictionary
        for (int i = 0; i < R; i++){
            st.put(new StringBuilder().append((char) i), i);
        }
        int code = R+1;  // R is codeword for EOF       //<----current codeword number

        input = readByte();

        while (input != null ) {
            StringBuilder s = st.longestPrefixOf(input);  // Find max prefix match s.

            //Keeps adding bytes until it does not match a codeword
            while (s.length() == input.length()) {
                next = readByte();
                input.append(next);
                s = st.longestPrefixOf(input);
            }
            BinaryStdOut.write(st.get(s), W);      // Print s's encoding.

            if (next != null && code < L) {    // Add s to symbol table.
                st.put(new StringBuilder(input.substring(0, s.length() + 1)), code++); //(input, code++); 
                if (code == L) {
                    if (W < 16) {
                        W++;
                        L = (int) Math.pow(2, W);
                    } else if (W == 16 && reset) {
                        System.err.println("Resetting dictionary...");
                        //reset the dictionary
                        st = new TSTmod<Integer>();
                        //re-add ASCII charaters
                        for (int i = 0; i < R; i++){
                            st.put(new StringBuilder().append((char) i), i);
                        }
                        code = R + 1;
                        W = 9;
                        L = 512;
                    }
                }
            }
            input = next;
        }
        BinaryStdOut.write(R, W);
        BinaryStdOut.close();
    } 

    public static void expand() {
        String[] st = new String[MAX];
        int i; // next available codeword value

        // initialize symbol table with all 1-character strings
        for (i = 0; i < R; i++)
            st[i] = "" + (char) i;
        st[i++] = "";                        // (unused) lookahead for EOF

        int codeword = BinaryStdIn.readInt(W);
        String val = st[codeword];

        while (true) {
            BinaryStdOut.write(val);

            codeword = BinaryStdIn.readInt(W);
            if (codeword == R) break;
            String s;
            if (i == codeword) {
                s = val + val.charAt(0);   // special case hack
            } else {
                s = st[codeword];
            }
            if (i < L) {
                st[i++] = val + s.charAt(0);
            }
            val = s;
            if (i + 1 == L && W < 16) {
                W++;
                L = (int) Math.pow(2, W);
            }

            if (i + 1 == L) {
                st[i++] = val + s.charAt(0);
                if (reset) {
                    BinaryStdOut.write(val);
                    System.err.println("Resetting the dictionary...");
                    st = new String[MAX];

                    // initialize symbol table with all 1-character strings
                    for (i = 0; i < R; i++)
                        st[i] = "" + (char) i;
                    st[i++] = "";                        // (unused) lookahead for EOF

                    W = 9;
                    L = 512;
                    codeword = BinaryStdIn.readInt(W);
                    val = st[codeword];
                } else {
                    BinaryStdOut.write(val);
                    codeword = BinaryStdIn.readInt(W);
                    while (codeword != R) {
                        BinaryStdOut.write(st[codeword]);
                        codeword = BinaryStdIn.readInt(W);
                    }
                    break;
                }
            }
        }
        BinaryStdOut.close();
    }

    //Reads in the next 8 bits.
    public static StringBuilder readByte() {
        StringBuilder readIn = new StringBuilder();

        try {
            readIn.append( (char) BinaryStdIn.readChar());
        } catch (Exception e) {  
        readIn = null; } //if there is an exception, at EOF

        return readIn;
    }

    public static void main(String[] args) {
        MAX = (int) Math.pow(2, 16) + 1;
        if (args[0].equals("-")){
            if (args[1].equals("r")) {
                reset = true;
                BinaryStdOut.write('1');
            } else{
                BinaryStdOut.write('0');
                reset = false;
            }
            compress();
        } else if (args[0].equals("+")){
            char c = BinaryStdIn.readChar();
            if (c == '1') reset = true;
            else reset = false;
            expand();
        }
        else throw new RuntimeException("Illegal command line argument");
    }

}