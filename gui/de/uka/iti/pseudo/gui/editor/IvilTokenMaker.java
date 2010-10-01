/* The following code was generated by JFlex 1.4.3 on 30.09.10 01:40 */

package de.uka.iti.pseudo.gui.editor;

import java.io.*;
import javax.swing.text.Segment;

import org.fife.ui.rsyntaxtextarea.*;

import de.uka.iti.pseudo.util.Log;


/**
 * This class is a scanner generated by 
 * <a href="http://www.jflex.de/">JFlex</a> 1.4.3
 * on 30.09.10 01:40 from the specification file
 * <tt>IvilTokenMaker.flex</tt>
 */
public class IvilTokenMaker extends AbstractJFlexTokenMaker {

  /** This character denotes the end of file */
  public static final int YYEOF = -1;

  /** initial size of the lookahead buffer */
  private static final int ZZ_BUFFERSIZE = 16384;

  /** lexical states */
  public static final int STRING = 6;
  public static final int YYINITIAL = 0;
  public static final int SLC = 4;
  public static final int MLC = 2;

  /**
   * ZZ_LEXSTATE[l] is the state in the DFA for the lexical state l
   * ZZ_LEXSTATE[l+1] is the state in the DFA for the lexical state l
   *                  at the beginning of a line
   * l is of the form l = 2*k, k a non negative integer
   */
  private static final int ZZ_LEXSTATE[] = { 
     0,  0,  1,  1,  2,  2,  3, 3
  };

  /** 
   * Translates characters to character classes
   */
  private static final String ZZ_CMAP_PACKED = 
    "\11\0\1\13\1\12\1\0\1\13\23\0\1\13\1\4\1\52\1\17"+
    "\1\5\1\6\1\4\1\7\1\14\1\16\1\15\1\4\1\0\1\47"+
    "\2\4\12\3\1\4\1\0\3\4\1\0\1\4\23\1\1\50\6\1"+
    "\1\11\1\10\1\11\1\4\1\2\1\0\1\25\1\34\1\40\1\30"+
    "\1\23\1\24\1\32\1\45\1\31\1\1\1\51\1\26\1\37\1\33"+
    "\1\36\1\41\1\44\1\21\1\27\1\20\1\22\1\43\1\42\1\35"+
    "\2\1\1\11\1\46\1\11\uff82\0";

  /** 
   * Translates characters to character classes
   */
  private static final char [] ZZ_CMAP = zzUnpackCMap(ZZ_CMAP_PACKED);

  /** 
   * Translates DFA states to action switch labels.
   */
  private static final int [] ZZ_ACTION = zzUnpackAction();

  private static final String ZZ_ACTION_PACKED_0 =
    "\4\0\1\1\1\2\1\3\1\4\4\1\1\5\1\6"+
    "\1\7\1\5\1\10\20\2\1\4\1\11\1\12\1\13"+
    "\3\12\1\14\1\12\1\15\1\0\1\16\1\0\1\17"+
    "\1\2\1\20\10\2\1\21\17\2\1\21\1\22\1\23"+
    "\7\2\1\21\25\2\1\24\11\2\1\21\31\2\1\21"+
    "\15\2";

  private static int [] zzUnpackAction() {
    int [] result = new int[155];
    int offset = 0;
    offset = zzUnpackAction(ZZ_ACTION_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackAction(String packed, int offset, int [] result) {
    int i = 0;       /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int count = packed.charAt(i++);
      int value = packed.charAt(i++);
      do result[j++] = value; while (--count > 0);
    }
    return j;
  }


  /** 
   * Translates a state to a row index in the transition table
   */
  private static final int [] ZZ_ROWMAP = zzUnpackRowMap();

  private static final String ZZ_ROWMAP_PACKED_0 =
    "\0\0\0\53\0\126\0\201\0\254\0\327\0\u0102\0\u012d"+
    "\0\u0158\0\u0183\0\u01ae\0\u01d9\0\254\0\254\0\u0204\0\u022f"+
    "\0\254\0\u025a\0\u0285\0\u02b0\0\u02db\0\u0306\0\u0331\0\u035c"+
    "\0\u0387\0\u03b2\0\u03dd\0\u0408\0\u0433\0\u045e\0\u0489\0\u04b4"+
    "\0\u04df\0\u050a\0\254\0\u0535\0\254\0\u0560\0\u058b\0\u05b6"+
    "\0\254\0\u05e1\0\254\0\u060c\0\u0637\0\u0662\0\u068d\0\u06b8"+
    "\0\254\0\u06e3\0\u070e\0\u0739\0\u0764\0\u078f\0\u07ba\0\u07e5"+
    "\0\u0810\0\u083b\0\u0866\0\u0891\0\u08bc\0\u08e7\0\u0912\0\u093d"+
    "\0\u0968\0\u0993\0\u09be\0\u09e9\0\u0a14\0\u0a3f\0\u0a6a\0\u0a95"+
    "\0\u0ac0\0\u012d\0\254\0\254\0\u0aeb\0\u0b16\0\u0b41\0\u0b6c"+
    "\0\u0b97\0\u0bc2\0\u0bed\0\327\0\u0c18\0\u0c43\0\u0c6e\0\u0c99"+
    "\0\u0cc4\0\u0cef\0\u0d1a\0\u0d45\0\u0d70\0\u0d9b\0\u0dc6\0\u0df1"+
    "\0\u0e1c\0\u0e47\0\u0e72\0\u0e9d\0\u0ec8\0\u0ef3\0\u0f1e\0\u0f49"+
    "\0\u0f74\0\327\0\u0f9f\0\u0fca\0\u0ff5\0\u1020\0\u104b\0\u1076"+
    "\0\u10a1\0\u10cc\0\u10f7\0\u1122\0\u114d\0\u1178\0\u11a3\0\u11ce"+
    "\0\u11f9\0\u1224\0\u124f\0\u127a\0\u12a5\0\u12d0\0\u12fb\0\u1326"+
    "\0\u1351\0\u137c\0\u13a7\0\u13d2\0\u13fd\0\u1428\0\u1453\0\u147e"+
    "\0\u14a9\0\u14d4\0\u14ff\0\u152a\0\u1555\0\u1580\0\u15ab\0\u15d6"+
    "\0\u1601\0\u162c\0\u1657\0\u1682\0\u16ad\0\u16d8\0\u1703\0\u172e"+
    "\0\u1759\0\u1784\0\u17af";

  private static int [] zzUnpackRowMap() {
    int [] result = new int[155];
    int offset = 0;
    offset = zzUnpackRowMap(ZZ_ROWMAP_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackRowMap(String packed, int offset, int [] result) {
    int i = 0;  /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int high = packed.charAt(i++) << 16;
      result[j++] = high | packed.charAt(i++);
    }
    return j;
  }

  /** 
   * The transition table of the DFA
   */
  private static final int [] ZZ_TRANS = zzUnpackTrans();

  private static final String ZZ_TRANS_PACKED_0 =
    "\1\5\2\6\1\7\1\10\1\11\1\12\1\13\1\14"+
    "\1\15\1\16\1\17\1\20\1\10\1\15\1\21\1\22"+
    "\1\23\1\24\1\25\1\26\1\27\1\6\1\30\1\31"+
    "\1\32\1\33\1\34\1\35\3\6\1\36\1\37\1\40"+
    "\2\6\1\41\1\42\1\10\2\6\1\43\12\44\1\45"+
    "\1\44\1\46\1\47\35\44\12\50\1\51\40\50\52\52"+
    "\1\53\54\0\3\6\14\0\26\6\2\0\2\6\4\0"+
    "\1\7\53\0\1\10\10\0\1\10\30\0\2\10\4\0"+
    "\2\6\2\0\1\54\12\0\26\6\2\0\2\6\2\0"+
    "\2\55\4\0\1\56\10\0\26\55\2\0\2\55\2\0"+
    "\2\57\15\0\26\57\2\0\2\57\2\0\2\6\15\0"+
    "\26\6\2\0\1\60\1\6\14\0\1\17\54\0\1\61"+
    "\36\0\3\6\14\0\1\6\1\62\3\6\1\63\20\6"+
    "\2\0\2\6\2\0\3\6\14\0\2\6\1\64\1\65"+
    "\22\6\2\0\2\6\2\0\3\6\14\0\13\6\1\66"+
    "\12\6\2\0\2\6\2\0\3\6\14\0\13\6\1\67"+
    "\12\6\2\0\2\6\2\0\3\6\14\0\2\6\1\70"+
    "\2\6\1\71\3\6\1\25\14\6\2\0\2\6\2\0"+
    "\3\6\14\0\7\6\1\72\1\67\4\6\1\73\10\6"+
    "\2\0\2\6\2\0\3\6\14\0\5\6\1\74\10\6"+
    "\1\75\7\6\2\0\1\6\1\76\2\0\3\6\14\0"+
    "\3\6\1\77\22\6\2\0\2\6\2\0\3\6\14\0"+
    "\13\6\1\100\12\6\2\0\2\6\2\0\3\6\14\0"+
    "\16\6\1\101\7\6\2\0\2\6\2\0\3\6\14\0"+
    "\3\6\1\102\12\6\1\103\7\6\2\0\2\6\2\0"+
    "\3\6\14\0\11\6\1\104\14\6\2\0\2\6\2\0"+
    "\3\6\14\0\6\6\1\105\17\6\2\0\2\6\2\0"+
    "\3\6\14\0\1\6\1\106\4\6\1\107\17\6\2\0"+
    "\2\6\2\0\3\6\14\0\25\6\1\110\2\0\2\6"+
    "\2\0\3\6\14\0\5\6\1\111\20\6\2\0\2\6"+
    "\5\0\1\10\10\0\1\10\30\0\1\10\1\112\3\0"+
    "\12\44\1\0\2\44\1\0\47\44\1\0\2\44\1\113"+
    "\35\44\16\0\1\114\34\0\12\50\1\0\40\50\52\52"+
    "\2\0\2\6\15\0\26\6\2\0\2\6\2\0\3\55"+
    "\14\0\26\55\2\0\2\55\2\0\2\55\15\0\26\55"+
    "\2\0\2\55\2\0\3\57\14\0\26\57\2\0\2\57"+
    "\2\0\1\6\1\115\1\6\14\0\26\6\2\0\2\6"+
    "\2\0\3\6\14\0\2\6\1\116\23\6\2\0\2\6"+
    "\2\0\3\6\14\0\12\6\1\117\13\6\2\0\2\6"+
    "\2\0\3\6\14\0\6\6\1\120\17\6\2\0\2\6"+
    "\2\0\3\6\14\0\17\6\1\121\1\6\1\122\4\6"+
    "\2\0\2\6\2\0\3\6\14\0\11\6\1\123\14\6"+
    "\2\0\2\6\2\0\3\6\14\0\10\6\1\124\15\6"+
    "\2\0\2\6\2\0\3\6\14\0\13\6\1\125\12\6"+
    "\2\0\2\6\2\0\3\6\14\0\6\6\1\126\17\6"+
    "\2\0\2\6\2\0\3\6\14\0\7\6\1\127\16\6"+
    "\2\0\2\6\2\0\3\6\14\0\11\6\1\130\14\6"+
    "\2\0\2\6\2\0\3\6\14\0\17\6\1\131\6\6"+
    "\2\0\2\6\2\0\3\6\14\0\1\6\1\103\1\132"+
    "\23\6\2\0\2\6\2\0\3\6\14\0\11\6\1\133"+
    "\14\6\2\0\2\6\2\0\3\6\14\0\7\6\1\134"+
    "\16\6\2\0\2\6\2\0\3\6\14\0\4\6\1\135"+
    "\13\6\1\136\5\6\2\0\2\6\2\0\3\6\14\0"+
    "\1\137\25\6\2\0\2\6\2\0\3\6\14\0\22\6"+
    "\1\140\3\6\2\0\2\6\2\0\3\6\14\0\1\124"+
    "\25\6\2\0\2\6\2\0\3\6\14\0\13\6\1\141"+
    "\12\6\2\0\2\6\2\0\3\6\14\0\16\6\1\142"+
    "\7\6\2\0\2\6\2\0\3\6\14\0\3\6\1\143"+
    "\12\6\1\144\7\6\2\0\2\6\2\0\3\6\14\0"+
    "\2\6\1\145\23\6\2\0\2\6\2\0\3\6\14\0"+
    "\3\6\1\146\22\6\2\0\2\6\2\0\3\6\14\0"+
    "\23\6\1\147\2\6\2\0\2\6\2\0\3\6\14\0"+
    "\3\6\1\150\1\6\1\151\20\6\2\0\2\6\2\0"+
    "\3\6\14\0\3\6\1\152\22\6\2\0\2\6\2\0"+
    "\3\6\14\0\7\6\1\124\16\6\2\0\2\6\2\0"+
    "\3\6\14\0\3\6\1\124\22\6\2\0\2\6\2\0"+
    "\3\6\14\0\16\6\1\153\7\6\2\0\2\6\2\0"+
    "\3\6\14\0\6\6\1\154\17\6\2\0\2\6\2\0"+
    "\3\6\14\0\24\6\1\155\1\6\2\0\2\6\2\0"+
    "\3\6\14\0\20\6\1\156\5\6\2\0\2\6\2\0"+
    "\3\6\14\0\7\6\1\116\16\6\2\0\2\6\2\0"+
    "\3\6\14\0\2\6\1\157\1\160\5\6\1\161\14\6"+
    "\2\0\2\6\2\0\3\6\14\0\16\6\1\162\7\6"+
    "\2\0\2\6\2\0\3\6\14\0\3\6\1\140\22\6"+
    "\2\0\2\6\2\0\3\6\14\0\1\6\1\163\24\6"+
    "\2\0\2\6\2\0\3\6\14\0\21\6\1\164\4\6"+
    "\2\0\2\6\2\0\3\6\14\0\20\6\1\165\5\6"+
    "\2\0\2\6\2\0\3\6\14\0\11\6\1\150\14\6"+
    "\2\0\2\6\2\0\3\6\14\0\6\6\1\166\17\6"+
    "\2\0\2\6\2\0\3\6\14\0\16\6\1\124\7\6"+
    "\2\0\2\6\2\0\3\6\14\0\12\6\1\167\13\6"+
    "\2\0\2\6\2\0\3\6\14\0\10\6\1\170\15\6"+
    "\2\0\2\6\2\0\3\6\14\0\7\6\1\131\16\6"+
    "\2\0\2\6\2\0\3\6\14\0\4\6\1\135\21\6"+
    "\2\0\2\6\2\0\3\6\14\0\12\6\1\171\1\6"+
    "\1\172\4\6\1\173\4\6\2\0\2\6\2\0\3\6"+
    "\14\0\12\6\1\174\13\6\2\0\2\6\2\0\3\6"+
    "\14\0\1\6\1\120\24\6\2\0\2\6\2\0\3\6"+
    "\14\0\16\6\1\175\7\6\2\0\2\6\2\0\3\6"+
    "\14\0\15\6\1\124\10\6\2\0\2\6\2\0\3\6"+
    "\14\0\6\6\1\176\17\6\2\0\2\6\2\0\3\6"+
    "\14\0\23\6\1\120\2\6\2\0\2\6\2\0\3\6"+
    "\14\0\5\6\1\177\20\6\2\0\2\6\2\0\3\6"+
    "\14\0\2\6\1\120\23\6\2\0\2\6\2\0\3\6"+
    "\14\0\1\200\25\6\2\0\2\6\2\0\3\6\14\0"+
    "\17\6\1\120\6\6\2\0\2\6\2\0\3\6\14\0"+
    "\1\6\1\103\24\6\2\0\2\6\2\0\3\6\14\0"+
    "\12\6\1\201\13\6\2\0\2\6\2\0\3\6\14\0"+
    "\17\6\1\124\6\6\2\0\2\6\2\0\3\6\14\0"+
    "\20\6\1\202\5\6\2\0\2\6\2\0\1\6\1\203"+
    "\1\6\14\0\26\6\2\0\2\6\2\0\3\6\14\0"+
    "\1\6\1\204\24\6\2\0\2\6\2\0\3\6\14\0"+
    "\2\6\1\205\23\6\2\0\2\6\2\0\3\6\14\0"+
    "\16\6\1\206\7\6\2\0\2\6\2\0\3\6\14\0"+
    "\3\6\1\207\22\6\2\0\2\6\2\0\3\6\14\0"+
    "\1\6\1\210\24\6\2\0\2\6\2\0\3\6\14\0"+
    "\6\6\1\211\17\6\2\0\2\6\2\0\3\6\14\0"+
    "\3\6\1\212\22\6\2\0\2\6\2\0\3\6\14\0"+
    "\11\6\1\213\14\6\2\0\2\6\2\0\3\6\14\0"+
    "\20\6\1\124\5\6\2\0\2\6\2\0\3\6\14\0"+
    "\6\6\1\124\17\6\2\0\2\6\2\0\3\6\14\0"+
    "\20\6\1\120\5\6\2\0\2\6\2\0\3\6\14\0"+
    "\11\6\1\214\14\6\2\0\2\6\2\0\3\6\14\0"+
    "\13\6\1\215\12\6\2\0\2\6\2\0\3\6\14\0"+
    "\3\6\1\216\22\6\2\0\2\6\2\0\3\6\14\0"+
    "\6\6\1\217\17\6\2\0\2\6\2\0\3\6\14\0"+
    "\11\6\1\220\14\6\2\0\2\6\2\0\3\6\14\0"+
    "\10\6\1\120\15\6\2\0\2\6\2\0\3\6\14\0"+
    "\5\6\1\176\20\6\2\0\2\6\2\0\3\6\14\0"+
    "\1\6\1\124\24\6\2\0\2\6\2\0\3\6\14\0"+
    "\5\6\1\162\20\6\2\0\2\6\2\0\3\6\14\0"+
    "\3\6\1\162\22\6\2\0\2\6\2\0\3\6\14\0"+
    "\1\6\1\221\24\6\2\0\2\6\2\0\3\6\14\0"+
    "\13\6\1\124\12\6\2\0\2\6\2\0\3\6\14\0"+
    "\16\6\1\213\7\6\2\0\2\6\2\0\3\6\14\0"+
    "\5\6\1\222\20\6\2\0\2\6\2\0\3\6\14\0"+
    "\6\6\1\223\17\6\2\0\2\6\2\0\3\6\14\0"+
    "\16\6\1\224\7\6\2\0\2\6\2\0\3\6\14\0"+
    "\21\6\1\156\4\6\2\0\2\6\2\0\3\6\14\0"+
    "\1\225\25\6\2\0\2\6\2\0\3\6\14\0\14\6"+
    "\1\64\11\6\2\0\2\6\2\0\3\6\14\0\11\6"+
    "\1\226\14\6\2\0\2\6\2\0\3\6\14\0\16\6"+
    "\1\227\7\6\2\0\2\6\2\0\3\6\14\0\11\6"+
    "\1\230\14\6\2\0\2\6\2\0\3\6\14\0\13\6"+
    "\1\120\12\6\2\0\2\6\2\0\3\6\14\0\21\6"+
    "\1\231\4\6\2\0\2\6\2\0\3\6\14\0\3\6"+
    "\1\117\22\6\2\0\2\6\2\0\3\6\14\0\11\6"+
    "\1\232\14\6\2\0\2\6\2\0\3\6\14\0\13\6"+
    "\1\233\12\6\2\0\2\6\2\0\3\6\14\0\23\6"+
    "\1\124\2\6\2\0\2\6\1\0";

  private static int [] zzUnpackTrans() {
    int [] result = new int[6106];
    int offset = 0;
    offset = zzUnpackTrans(ZZ_TRANS_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackTrans(String packed, int offset, int [] result) {
    int i = 0;       /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int count = packed.charAt(i++);
      int value = packed.charAt(i++);
      value--;
      do result[j++] = value; while (--count > 0);
    }
    return j;
  }


  /* error codes */
  private static final int ZZ_UNKNOWN_ERROR = 0;
  private static final int ZZ_NO_MATCH = 1;
  private static final int ZZ_PUSHBACK_2BIG = 2;

  /* error messages for the codes above */
  private static final String ZZ_ERROR_MSG[] = {
    "Unkown internal scanner error",
    "Error: could not match input",
    "Error: pushback value was too large"
  };

  /**
   * ZZ_ATTRIBUTE[aState] contains the attributes of state <code>aState</code>
   */
  private static final int [] ZZ_ATTRIBUTE = zzUnpackAttribute();

  private static final String ZZ_ATTRIBUTE_PACKED_0 =
    "\4\0\1\11\7\1\2\11\2\1\1\11\21\1\1\11"+
    "\1\1\1\11\3\1\1\11\1\1\1\11\1\0\1\1"+
    "\1\0\2\1\1\11\31\1\2\11\117\1";

  private static int [] zzUnpackAttribute() {
    int [] result = new int[155];
    int offset = 0;
    offset = zzUnpackAttribute(ZZ_ATTRIBUTE_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackAttribute(String packed, int offset, int [] result) {
    int i = 0;       /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int count = packed.charAt(i++);
      int value = packed.charAt(i++);
      do result[j++] = value; while (--count > 0);
    }
    return j;
  }

  /** the input device */
  private java.io.Reader zzReader;

  /** the current state of the DFA */
  private int zzState;

  /** the current lexical state */
  private int zzLexicalState = YYINITIAL;

  /** this buffer contains the current text to be matched and is
      the source of the yytext() string */
  private char zzBuffer[]; // = new char[ZZ_BUFFERSIZE];

  /** the textposition at the last accepting state */
  private int zzMarkedPos;

  /** the current text position in the buffer */
  private int zzCurrentPos;

  /** startRead marks the beginning of the yytext() string in the buffer */
  private int zzStartRead;

  /** endRead marks the last character in the buffer, that has been read
      from input */
  private int zzEndRead;

  /** number of newlines encountered up to the start of the matched text */
  private int yyline;

  /** the number of characters up to the start of the matched text */
  private int yychar;

  /**
   * the number of characters from the last newline up to the start of the 
   * matched text
   */
  private int yycolumn;

  /** 
   * zzAtBOL == true <=> the scanner is currently at the beginning of a line
   */
  private boolean zzAtBOL = true;

  /** zzAtEOF == true <=> the scanner is at the EOF */
  private boolean zzAtEOF;

  /** denotes if the user-EOF-code has already been executed */
  private boolean zzEOFDone;

  /* user code: */
        /**
         * Token type specific to IvilTokenMaker; denotes an unterminated String
         */
        public static final int INTERNAL_STRING = -2;
        
	/**
	 * Token type specific to IvilTokenMaker; denotes a line ending
	 * with an unterminated "(*" comment.
	 */
	public static final int INTERNAL_MLC_LEVEL = -1000;

	/**
	 * Constructor.  This must be here because JFlex does not generate a
	 * no-parameter constructor.
	 */
	public IvilTokenMaker() {
	}
	
	private int commentLevel = 0;

	/**
	 * Adds the token specified to the current linked list of tokens as an
	 * "end token;" that is, at <code>zzMarkedPos</code>.
	 *
	 * @param tokenType The token's type.
	 */
	private void addEndToken(int tokenType) {
		addToken(zzMarkedPos,zzMarkedPos, tokenType);
	}


	/**
	 * Adds the token specified to the current linked list of tokens.
	 *
	 * @param tokenType The token's type.
	 * @see #addToken(int, int, int)
	 */
//	private void addHyperlinkToken(int start, int end, int tokenType) {
//		int so = start + offsetShift;
//		addToken(zzBuffer, start,end, tokenType, so, true);
//	}


	/**
	 * Adds the token specified to the current linked list of tokens.
	 *
	 * @param tokenType The token's type.
	 */
	private void addToken(int tokenType) {
		addToken(zzStartRead, zzMarkedPos-1, tokenType);
	}


	/**
	 * Adds the token specified to the current linked list of tokens.
	 *
	 * @param tokenType The token's type.
	 * @see #addHyperlinkToken(int, int, int)
	 */
	private void addToken(int start, int end, int tokenType) {
		int so = start + offsetShift;
		addToken(zzBuffer, start,end, tokenType, so, false);
	}


	/**
	 * Adds the token specified to the current linked list of tokens.
	 *
	 * @param array The character array.
	 * @param start The starting offset in the array.
	 * @param end The ending offset in the array.
	 * @param tokenType The token's type.
	 * @param startOffset The offset in the document at which this token
	 *                    occurs.
	 * @param hyperlink Whether this token is a hyperlink.
	 */
	public void addToken(char[] array, int start, int end, int tokenType,
						int startOffset, boolean hyperlink) {
		super.addToken(array, start,end, tokenType, startOffset, hyperlink);
		zzStartRead = zzMarkedPos;
	}


	/**
	 * Returns the text to place at the beginning and end of a
	 * line to "comment" it in a this programming language.
	 *
	 * @return The start and end strings to add to a line to "comment"
	 *         it out.
	 */
	public String[] getLineCommentStartAndEnd() {
		return new String[] { "# ", null };
	}


	/**
	 * Returns the first token in the linked list of tokens generated
	 * from <code>text</code>.  This method must be implemented by
	 * subclasses so they can correctly implement syntax highlighting.
	 *
	 * @param text The text from which to get tokens.
	 * @param initialTokenType The token type we should start with.
	 * @param startOffset The offset into the document at which
	 *        <code>text</code> starts.
	 * @return The first <code>Token</code> in a linked list representing
	 *         the syntax highlighted text.
	 */
	public Token getTokenList(Segment text, int initialTokenType, int startOffset) {

		resetTokenList();
		this.offsetShift = -text.offset + startOffset;
                this.commentLevel = 0;
                
		// Start off in the proper state.
		int state = Token.NULL;
		if(initialTokenType <= INTERNAL_MLC_LEVEL) {
		      state = MLC;
                      commentLevel = (INTERNAL_MLC_LEVEL - initialTokenType);
                      start = text.offset;
		} else if(initialTokenType == INTERNAL_STRING) {
		      state = STRING;
		      start = text.offset;
		} else {
		      state = Token.NULL;
		}

		s = text;
		try {
			yyreset(zzReader);
			yybegin(state);
			return yylex();
		} catch (IOException ioe) {
			ioe.printStackTrace();
			return new DefaultToken();
		}

	}


	/**
	 * Refills the input buffer.
	 *
	 * @return      <code>true</code> if EOF was reached, otherwise
	 *              <code>false</code>.
	 * @exception   IOException  if any I/O-Error occurs.
	 */
	private boolean zzRefill() throws java.io.IOException {
		return zzCurrentPos>=s.offset+s.count;
	}


	/**
	 * Resets the scanner to read from a new input stream.
	 * Does not close the old reader.
	 *
	 * All internal variables are reset, the old input stream 
	 * <b>cannot</b> be reused (internal buffer is discarded and lost).
	 * Lexical state is set to <tt>YY_INITIAL</tt>.
	 *
	 * @param reader   the new input stream 
	 */
	public final void yyreset(java.io.Reader reader) throws java.io.IOException {
		// 's' has been updated.
		zzBuffer = s.array;
		/*
		 * We replaced the line below with the two below it because zzRefill
		 * no longer "refills" the buffer (since the way we do it, it's always
		 * "full" the first time through, since it points to the segment's
		 * array).  So, we assign zzEndRead here.
		 */
		//zzStartRead = zzEndRead = s.offset;
		zzStartRead = s.offset;
		zzEndRead = zzStartRead + s.count - 1;
		zzCurrentPos = zzMarkedPos/* = zzPushbackPos*/ = s.offset;
		zzLexicalState = YYINITIAL;
		zzReader = reader;
		zzAtBOL  = true;
		zzAtEOF  = false;
	}




  /**
   * Creates a new scanner
   * There is also a java.io.InputStream version of this constructor.
   *
   * @param   in  the java.io.Reader to read input from.
   */
  public IvilTokenMaker(java.io.Reader in) {
    this.zzReader = in;
  }

  /**
   * Creates a new scanner.
   * There is also java.io.Reader version of this constructor.
   *
   * @param   in  the java.io.Inputstream to read input from.
   */
  public IvilTokenMaker(java.io.InputStream in) {
    this(new java.io.InputStreamReader(in));
  }

  /** 
   * Unpacks the compressed character translation table.
   *
   * @param packed   the packed character translation table
   * @return         the unpacked character translation table
   */
  private static char [] zzUnpackCMap(String packed) {
    char [] map = new char[0x10000];
    int i = 0;  /* index in packed string  */
    int j = 0;  /* index in unpacked array */
    while (i < 130) {
      int  count = packed.charAt(i++);
      char value = packed.charAt(i++);
      do map[j++] = value; while (--count > 0);
    }
    return map;
  }


  /**
   * Closes the input stream.
   */
  public final void yyclose() throws java.io.IOException {
    zzAtEOF = true;            /* indicate end of file */
    zzEndRead = zzStartRead;  /* invalidate buffer    */

    if (zzReader != null)
      zzReader.close();
  }


  /**
   * Returns the current lexical state.
   */
  public final int yystate() {
    return zzLexicalState;
  }


  /**
   * Enters a new lexical state
   *
   * @param newState the new lexical state
   */
  public final void yybegin(int newState) {
    zzLexicalState = newState;
  }


  /**
   * Returns the text matched by the current regular expression.
   */
  public final String yytext() {
    return new String( zzBuffer, zzStartRead, zzMarkedPos-zzStartRead );
  }


  /**
   * Returns the character at position <tt>pos</tt> from the 
   * matched text. 
   * 
   * It is equivalent to yytext().charAt(pos), but faster
   *
   * @param pos the position of the character to fetch. 
   *            A value from 0 to yylength()-1.
   *
   * @return the character at position pos
   */
  public final char yycharat(int pos) {
    return zzBuffer[zzStartRead+pos];
  }


  /**
   * Returns the length of the matched text region.
   */
  public final int yylength() {
    return zzMarkedPos-zzStartRead;
  }


  /**
   * Reports an error that occured while scanning.
   *
   * In a wellformed scanner (no or only correct usage of 
   * yypushback(int) and a match-all fallback rule) this method 
   * will only be called with things that "Can't Possibly Happen".
   * If this method is called, something is seriously wrong
   * (e.g. a JFlex bug producing a faulty scanner etc.).
   *
   * Usual syntax/scanner level error handling should be done
   * in error fallback rules.
   *
   * @param   errorCode  the code of the errormessage to display
   */
  private void zzScanError(int errorCode) {
    String message;
    try {
      message = ZZ_ERROR_MSG[errorCode];
    }
    catch (ArrayIndexOutOfBoundsException e) {
      message = ZZ_ERROR_MSG[ZZ_UNKNOWN_ERROR];
    }

    throw new Error(message);
  } 


  /**
   * Pushes the specified amount of characters back into the input stream.
   *
   * They will be read again by then next call of the scanning method
   *
   * @param number  the number of characters to be read again.
   *                This number must not be greater than yylength()!
   */
  public void yypushback(int number)  {
    if ( number > yylength() )
      zzScanError(ZZ_PUSHBACK_2BIG);

    zzMarkedPos -= number;
  }


  /**
   * Resumes scanning until the next regular expression is matched,
   * the end of input is encountered or an I/O-Error occurs.
   *
   * @return      the next token
   * @exception   java.io.IOException  if any I/O-Error occurs
   */
  public org.fife.ui.rsyntaxtextarea.Token yylex() throws java.io.IOException {
    int zzInput;
    int zzAction;

    // cached fields:
    int zzCurrentPosL;
    int zzMarkedPosL;
    int zzEndReadL = zzEndRead;
    char [] zzBufferL = zzBuffer;
    char [] zzCMapL = ZZ_CMAP;

    int [] zzTransL = ZZ_TRANS;
    int [] zzRowMapL = ZZ_ROWMAP;
    int [] zzAttrL = ZZ_ATTRIBUTE;

    while (true) {
      zzMarkedPosL = zzMarkedPos;

      zzAction = -1;

      zzCurrentPosL = zzCurrentPos = zzStartRead = zzMarkedPosL;
  
      zzState = ZZ_LEXSTATE[zzLexicalState];


      zzForAction: {
        while (true) {
    
          if (zzCurrentPosL < zzEndReadL)
            zzInput = zzBufferL[zzCurrentPosL++];
          else if (zzAtEOF) {
            zzInput = YYEOF;
            break zzForAction;
          }
          else {
            // store back cached positions
            zzCurrentPos  = zzCurrentPosL;
            zzMarkedPos   = zzMarkedPosL;
            boolean eof = zzRefill();
            // get translated positions and possibly new buffer
            zzCurrentPosL  = zzCurrentPos;
            zzMarkedPosL   = zzMarkedPos;
            zzBufferL      = zzBuffer;
            zzEndReadL     = zzEndRead;
            if (eof) {
              zzInput = YYEOF;
              break zzForAction;
            }
            else {
              zzInput = zzBufferL[zzCurrentPosL++];
            }
          }
          int zzNext = zzTransL[ zzRowMapL[zzState] + zzCMapL[zzInput] ];
          if (zzNext == -1) break zzForAction;
          zzState = zzNext;

          int zzAttributes = zzAttrL[zzState];
          if ( (zzAttributes & 1) == 1 ) {
            zzAction = zzState;
            zzMarkedPosL = zzCurrentPosL;
            if ( (zzAttributes & 8) == 8 ) break zzForAction;
          }

        }
      }

      // store back cached position
      zzMarkedPos = zzMarkedPosL;

      switch (zzAction < 0 ? zzAction : ZZ_ACTION[zzAction]) {
        case 6: 
          { addNullToken(); return firstToken;
          }
        case 21: break;
        case 13: 
          { addToken(start, zzStartRead, Token.LITERAL_STRING_DOUBLE_QUOTE); yybegin(YYINITIAL);
          }
        case 22: break;
        case 7: 
          { addToken(Token.WHITESPACE);
          }
        case 23: break;
        case 19: 
          { commentLevel --;
	Log.log(Log.VERBOSE, start + " decrements to " + commentLevel);
	              if(commentLevel == 0) {
	                yybegin(YYINITIAL); 
	                addToken(start,zzStartRead+1, Token.COMMENT_MULTILINE); 
	              }
          }
        case 24: break;
        case 17: 
          { addToken(Token.RESERVED_WORD);
          }
        case 25: break;
        case 5: 
          { addToken(Token.SEPARATOR);
          }
        case 26: break;
        case 14: 
          { addToken(Token.VARIABLE);
          }
        case 27: break;
        case 2: 
          { addToken(Token.IDENTIFIER);
          }
        case 28: break;
        case 12: 
          { addToken(start,zzStartRead-1, Token.COMMENT_EOL); addNullToken(); return firstToken;
          }
        case 29: break;
        case 16: 
          { start = zzMarkedPos-2; yybegin(MLC);commentLevel++; Log.log(Log.VERBOSE, start + " increments to " + commentLevel);
          }
        case 30: break;
        case 15: 
          { addToken(Token.DATA_TYPE);
          }
        case 31: break;
        case 1: 
          { addToken(Token.ERROR_CHAR);
          }
        case 32: break;
        case 20: 
          { addToken(Token.LITERAL_BOOLEAN);
          }
        case 33: break;
        case 8: 
          { start = zzMarkedPos-1; yybegin(SLC);
          }
        case 34: break;
        case 11: 
          { addToken(start,zzStartRead-1, Token.COMMENT_MULTILINE);
                      int leveledState = (INTERNAL_MLC_LEVEL - commentLevel);
                      Log.log(Log.VERBOSE, start + " ends with level " + leveledState);
                      addEndToken(leveledState);
                      return firstToken;
          }
        case 35: break;
        case 9: 
          { start = zzMarkedPos-1; yybegin(STRING);
          }
        case 36: break;
        case 3: 
          { addToken(Token.LITERAL_NUMBER_DECIMAL_INT);
          }
        case 37: break;
        case 4: 
          { addToken(Token.OPERATOR);
          }
        case 38: break;
        case 18: 
          { commentLevel ++;
          }
        case 39: break;
        case 10: 
          { 
          }
        case 40: break;
        default: 
          if (zzInput == YYEOF && zzStartRead == zzCurrentPos) {
            zzAtEOF = true;
            switch (zzLexicalState) {
            case STRING: {
              addToken(start, zzStartRead-1, Token.LITERAL_STRING_DOUBLE_QUOTE); 
                       addEndToken(INTERNAL_STRING); return firstToken;
            }
            case 156: break;
            case YYINITIAL: {
              addNullToken(); return firstToken;
            }
            case 157: break;
            case SLC: {
              addToken(start,zzStartRead-1, Token.COMMENT_EOL); addNullToken(); return firstToken;
            }
            case 158: break;
            case MLC: {
              addToken(start,zzStartRead-1, Token.COMMENT_MULTILINE);
                      int leveledState = (INTERNAL_MLC_LEVEL - commentLevel);
                      Log.log(Log.VERBOSE, start + " ends with level " + leveledState);
                      addEndToken(leveledState);
                      return firstToken;
            }
            case 159: break;
            default:
            return null;
            }
          } 
          else {
            zzScanError(ZZ_NO_MATCH);
          }
      }
    }
  }


}