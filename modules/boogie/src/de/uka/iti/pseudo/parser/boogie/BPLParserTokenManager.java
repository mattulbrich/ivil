/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.parser.boogie;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.LinkedList;
import java.util.List;
import de.uka.iti.pseudo.parser.boogie.ast.*;
import de.uka.iti.pseudo.parser.boogie.ast.type.*;
import de.uka.iti.pseudo.parser.boogie.ast.expression.*;
// used for main
import de.uka.iti.pseudo.environment.boogie.EnvironmentCreationState;

/** Token Manager. */
public class BPLParserTokenManager implements BPLParserConstants
{
   int commentNestingDepth ;

  /** Debug output. */
  public  java.io.PrintStream debugStream = System.out;
  /** Set debug output. */
  public  void setDebugStream(java.io.PrintStream ds) { debugStream = ds; }
private final int jjStopStringLiteralDfa_0(int pos, long active0, long active1)
{
   switch (pos)
   {
      case 0:
         if ((active0 & 0x2000000000000L) != 0L || (active1 & 0x1000L) != 0L)
            return 47;
         if ((active0 & 0x800L) != 0L || (active1 & 0x400000L) != 0L)
            return 0;
         if ((active0 & 0x1bdbd7eb38000L) != 0L)
         {
            jjmatchedKind = 68;
            return 52;
         }
         if ((active0 & 0x400081000000L) != 0L)
         {
            jjmatchedKind = 68;
            return 30;
         }
         if ((active0 & 0x4200400000L) != 0L)
         {
            jjmatchedKind = 68;
            return 37;
         }
         if ((active1 & 0x1000000L) != 0L)
            return 15;
         if ((active1 & 0x44000L) != 0L)
            return 59;
         if ((active1 & 0x30000L) != 0L)
            return 8;
         if ((active0 & 0x20000040000L) != 0L)
         {
            jjmatchedKind = 68;
            return 3;
         }
         if ((active1 & 0x8000L) != 0L)
            return 23;
         return -1;
      case 1:
         if ((active0 & 0x1ffefffb78000L) != 0L)
         {
            jjmatchedKind = 68;
            jjmatchedPos = 1;
            return 52;
         }
         if ((active0 & 0x1000000000L) != 0L)
            return 52;
         if ((active1 & 0x20000L) != 0L)
            return 7;
         if ((active0 & 0x400000L) != 0L)
         {
            jjmatchedKind = 68;
            jjmatchedPos = 1;
            return 36;
         }
         return -1;
      case 2:
         if ((active0 & 0xffeffff50000L) != 0L)
         {
            jjmatchedKind = 68;
            jjmatchedPos = 2;
            return 52;
         }
         if ((active0 & 0x1000000028000L) != 0L)
            return 52;
         return -1;
      case 3:
         if ((active0 & 0x5f8b77f10000L) != 0L)
         {
            jjmatchedKind = 68;
            jjmatchedPos = 3;
            return 52;
         }
         if ((active0 & 0xa06488040000L) != 0L)
            return 52;
         return -1;
      case 4:
         if ((active0 & 0x528004110000L) != 0L)
            return 52;
         if ((active0 & 0xd0b73e00000L) != 0L)
         {
            jjmatchedKind = 68;
            jjmatchedPos = 4;
            return 52;
         }
         return -1;
      case 5:
         if ((active0 & 0x10371c00000L) != 0L)
         {
            if (jjmatchedPos != 5)
            {
               jjmatchedKind = 68;
               jjmatchedPos = 5;
            }
            return 52;
         }
         if ((active0 & 0xc0802200000L) != 0L)
            return 52;
         return -1;
      case 6:
         if ((active0 & 0x10171800000L) != 0L)
         {
            jjmatchedKind = 68;
            jjmatchedPos = 6;
            return 52;
         }
         if ((active0 & 0x202400000L) != 0L)
            return 52;
         return -1;
      case 7:
         if ((active0 & 0x141800000L) != 0L)
            return 52;
         if ((active0 & 0x10030000000L) != 0L)
         {
            jjmatchedKind = 68;
            jjmatchedPos = 7;
            return 52;
         }
         return -1;
      case 8:
         if ((active0 & 0x20000000L) != 0L)
         {
            jjmatchedKind = 68;
            jjmatchedPos = 8;
            return 52;
         }
         if ((active0 & 0x10010000000L) != 0L)
            return 52;
         return -1;
      case 9:
         if ((active0 & 0x20000000L) != 0L)
         {
            jjmatchedKind = 68;
            jjmatchedPos = 9;
            return 52;
         }
         return -1;
      case 10:
         if ((active0 & 0x20000000L) != 0L)
         {
            jjmatchedKind = 68;
            jjmatchedPos = 10;
            return 52;
         }
         return -1;
      case 11:
         if ((active0 & 0x20000000L) != 0L)
         {
            jjmatchedKind = 68;
            jjmatchedPos = 11;
            return 52;
         }
         return -1;
      case 12:
         if ((active0 & 0x20000000L) != 0L)
         {
            jjmatchedKind = 68;
            jjmatchedPos = 12;
            return 52;
         }
         return -1;
      default :
         return -1;
   }
}
private final int jjStartNfa_0(int pos, long active0, long active1)
{
   return jjMoveNfa_0(jjStopStringLiteralDfa_0(pos, active0, active1), pos + 1);
}
private int jjStopAtPos(int pos, int kind)
{
   jjmatchedKind = kind;
   jjmatchedPos = pos;
   return pos + 1;
}
private int jjMoveStringLiteralDfa0_0()
{
   switch(curChar)
   {
      case 37:
         return jjStopAtPos(0, 87);
      case 40:
         return jjStopAtPos(0, 74);
      case 41:
         return jjStopAtPos(0, 75);
      case 42:
         return jjStopAtPos(0, 59);
      case 43:
         jjmatchedKind = 84;
         return jjMoveStringLiteralDfa1_0(0x0L, 0x80000L);
      case 44:
         return jjStopAtPos(0, 77);
      case 45:
         return jjStopAtPos(0, 85);
      case 47:
         jjmatchedKind = 86;
         return jjMoveStringLiteralDfa1_0(0x800L, 0x0L);
      case 58:
         jjmatchedKind = 76;
         return jjMoveStringLiteralDfa1_0(0x2000000000000L, 0x0L);
      case 59:
         return jjStopAtPos(0, 73);
      case 60:
         jjmatchedKind = 78;
         return jjMoveStringLiteralDfa1_0(0x0L, 0x40000L);
      case 61:
         jjmatchedKind = 80;
         return jjMoveStringLiteralDfa1_0(0x0L, 0x20000L);
      case 62:
         return jjStartNfaWithStates_0(0, 79, 23);
      case 91:
         return jjStopAtPos(0, 67);
      case 93:
         return jjStopAtPos(0, 66);
      case 97:
         return jjMoveStringLiteralDfa1_0(0xc0004000000L, 0x0L);
      case 98:
         return jjMoveStringLiteralDfa1_0(0x20000040000L, 0x0L);
      case 99:
         return jjMoveStringLiteralDfa1_0(0x200000900000L, 0x0L);
      case 101:
         return jjMoveStringLiteralDfa1_0(0x4200400000L, 0x0L);
      case 102:
         return jjMoveStringLiteralDfa1_0(0x400081000000L, 0x0L);
      case 103:
         return jjMoveStringLiteralDfa1_0(0x400000000L, 0x0L);
      case 104:
         return jjMoveStringLiteralDfa1_0(0x100000000000L, 0x0L);
      case 105:
         return jjMoveStringLiteralDfa1_0(0x11020020000L, 0x0L);
      case 109:
         return jjMoveStringLiteralDfa1_0(0x40000000L, 0x0L);
      case 111:
         return jjMoveStringLiteralDfa1_0(0x1000000000000L, 0x0L);
      case 112:
         return jjMoveStringLiteralDfa1_0(0x10000000L, 0x0L);
      case 114:
         return jjMoveStringLiteralDfa1_0(0x902000000L, 0x0L);
      case 116:
         return jjMoveStringLiteralDfa1_0(0x802008000000L, 0x0L);
      case 117:
         return jjMoveStringLiteralDfa1_0(0x200000L, 0x0L);
      case 118:
         return jjMoveStringLiteralDfa1_0(0x8000L, 0x0L);
      case 119:
         return jjMoveStringLiteralDfa1_0(0x8000010000L, 0x0L);
      case 123:
         return jjStopAtPos(0, 65);
      case 124:
         return jjMoveStringLiteralDfa1_0(0x0L, 0x1000000L);
      case 125:
         jjmatchedKind = 64;
         return jjMoveStringLiteralDfa1_0(0x0L, 0x2000000L);
      default :
         return jjMoveNfa_0(2, 0);
   }
}
private int jjMoveStringLiteralDfa1_0(long active0, long active1)
{
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(0, active0, active1);
      return 1;
   }
   switch(curChar)
   {
      case 42:
         if ((active0 & 0x800L) != 0L)
            return jjStopAtPos(1, 11);
         break;
      case 43:
         if ((active1 & 0x80000L) != 0L)
            return jjStopAtPos(1, 83);
         break;
      case 58:
         if ((active1 & 0x40000L) != 0L)
            return jjStopAtPos(1, 82);
         break;
      case 61:
         if ((active0 & 0x2000000000000L) != 0L)
            return jjStopAtPos(1, 49);
         else if ((active1 & 0x20000L) != 0L)
            return jjStartNfaWithStates_0(1, 81, 7);
         break;
      case 97:
         return jjMoveStringLiteralDfa2_0(active0, 0x700000008000L, active1, 0L);
      case 101:
         return jjMoveStringLiteralDfa2_0(active0, 0x902000000L, active1, 0L);
      case 102:
         if ((active0 & 0x1000000000L) != 0L)
            return jjStartNfaWithStates_0(1, 36, 52);
         break;
      case 104:
         return jjMoveStringLiteralDfa2_0(active0, 0xa000010000L, active1, 0L);
      case 108:
         return jjMoveStringLiteralDfa2_0(active0, 0x1004000000000L, active1, 0L);
      case 109:
         return jjMoveStringLiteralDfa2_0(active0, 0x20000000L, active1, 0L);
      case 110:
         return jjMoveStringLiteralDfa2_0(active0, 0x10200220000L, active1, 0L);
      case 111:
         return jjMoveStringLiteralDfa2_0(active0, 0x440940000L, active1, 0L);
      case 114:
         return jjMoveStringLiteralDfa2_0(active0, 0x820090000000L, active1, 0L);
      case 115:
         return jjMoveStringLiteralDfa2_0(active0, 0xc0000000000L, active1, 0L);
      case 117:
         return jjMoveStringLiteralDfa2_0(active0, 0x1000000L, active1, 0L);
      case 120:
         return jjMoveStringLiteralDfa2_0(active0, 0x4400000L, active1, 0L);
      case 121:
         return jjMoveStringLiteralDfa2_0(active0, 0x8000000L, active1, 0L);
      case 123:
         if ((active1 & 0x1000000L) != 0L)
            return jjStopAtPos(1, 88);
         break;
      case 124:
         if ((active1 & 0x2000000L) != 0L)
            return jjStopAtPos(1, 89);
         break;
      default :
         break;
   }
   return jjStartNfa_0(0, active0, active1);
}
private int jjMoveStringLiteralDfa2_0(long old0, long active0, long old1, long active1)
{
   if (((active0 &= old0) | (active1 &= old1)) == 0L)
      return jjStartNfa_0(0, old0, old1);
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(1, active0, 0L);
      return 2;
   }
   switch(curChar)
   {
      case 100:
         if ((active0 & 0x1000000000000L) != 0L)
            return jjStartNfaWithStates_0(2, 48, 52);
         return jjMoveStringLiteralDfa3_0(active0, 0x40000000L);
      case 101:
         return jjMoveStringLiteralDfa3_0(active0, 0x22080010000L);
      case 105:
         return jjMoveStringLiteralDfa3_0(active0, 0x8004200000L);
      case 108:
         return jjMoveStringLiteralDfa3_0(active0, 0x600000000000L);
      case 109:
         return jjMoveStringLiteralDfa3_0(active0, 0x800000L);
      case 110:
         return jjMoveStringLiteralDfa3_0(active0, 0x1100000L);
      case 111:
         return jjMoveStringLiteralDfa3_0(active0, 0x10040000L);
      case 112:
         return jjMoveStringLiteralDfa3_0(active0, 0x28000000L);
      case 113:
         return jjMoveStringLiteralDfa3_0(active0, 0x100000000L);
      case 114:
         if ((active0 & 0x8000L) != 0L)
            return jjStartNfaWithStates_0(2, 15, 52);
         break;
      case 115:
         return jjMoveStringLiteralDfa3_0(active0, 0xc4200000000L);
      case 116:
         if ((active0 & 0x20000L) != 0L)
            return jjStartNfaWithStates_0(2, 17, 52);
         return jjMoveStringLiteralDfa3_0(active0, 0xc02400000L);
      case 117:
         return jjMoveStringLiteralDfa3_0(active0, 0x800000000000L);
      case 118:
         return jjMoveStringLiteralDfa3_0(active0, 0x110000000000L);
      default :
         break;
   }
   return jjStartNfa_0(1, active0, 0L);
}
private int jjMoveStringLiteralDfa3_0(long old0, long active0)
{
   if (((active0 &= old0)) == 0L)
      return jjStartNfa_0(1, old0, 0L);
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(2, active0, 0L);
      return 3;
   }
   switch(curChar)
   {
      case 97:
         return jjMoveStringLiteralDfa4_0(active0, 0x30000000000L);
      case 99:
         return jjMoveStringLiteralDfa4_0(active0, 0x11000000L);
      case 101:
         if ((active0 & 0x8000000L) != 0L)
            return jjStartNfaWithStates_0(3, 27, 52);
         else if ((active0 & 0x80000000L) != 0L)
            return jjStartNfaWithStates_0(3, 31, 52);
         else if ((active0 & 0x4000000000L) != 0L)
            return jjStartNfaWithStates_0(3, 38, 52);
         else if ((active0 & 0x800000000000L) != 0L)
            return jjStartNfaWithStates_0(3, 47, 52);
         return jjMoveStringLiteralDfa4_0(active0, 0x40000400000L);
      case 105:
         return jjMoveStringLiteralDfa4_0(active0, 0x40000000L);
      case 108:
         if ((active0 & 0x40000L) != 0L)
            return jjStartNfaWithStates_0(3, 18, 52);
         else if ((active0 & 0x200000000000L) != 0L)
            return jjStartNfaWithStates_0(3, 45, 52);
         return jjMoveStringLiteralDfa4_0(active0, 0x8020000000L);
      case 110:
         if ((active0 & 0x2000000000L) != 0L)
            return jjStartNfaWithStates_0(3, 37, 52);
         break;
      case 111:
         if ((active0 & 0x400000000L) != 0L)
            return jjStartNfaWithStates_0(3, 34, 52);
         return jjMoveStringLiteralDfa4_0(active0, 0x100004000000L);
      case 112:
         return jjMoveStringLiteralDfa4_0(active0, 0x800000L);
      case 113:
         return jjMoveStringLiteralDfa4_0(active0, 0x200000L);
      case 114:
         return jjMoveStringLiteralDfa4_0(active0, 0x10000L);
      case 115:
         return jjMoveStringLiteralDfa4_0(active0, 0x400000100000L);
      case 117:
         return jjMoveStringLiteralDfa4_0(active0, 0x80b02000000L);
      default :
         break;
   }
   return jjStartNfa_0(2, active0, 0L);
}
private int jjMoveStringLiteralDfa4_0(long old0, long active0)
{
   if (((active0 &= old0)) == 0L)
      return jjStartNfa_0(2, old0, 0L);
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(3, active0, 0L);
      return 4;
   }
   switch(curChar)
   {
      case 99:
         if ((active0 & 0x100000000000L) != 0L)
            return jjStartNfaWithStates_0(4, 44, 52);
         break;
      case 101:
         if ((active0 & 0x10000L) != 0L)
            return jjStartNfaWithStates_0(4, 16, 52);
         else if ((active0 & 0x8000000000L) != 0L)
            return jjStartNfaWithStates_0(4, 39, 52);
         else if ((active0 & 0x400000000000L) != 0L)
            return jjStartNfaWithStates_0(4, 46, 52);
         return jjMoveStringLiteralDfa5_0(active0, 0x30000000L);
      case 102:
         return jjMoveStringLiteralDfa5_0(active0, 0x40000000L);
      case 105:
         return jjMoveStringLiteralDfa5_0(active0, 0x100000000L);
      case 107:
         if ((active0 & 0x20000000000L) != 0L)
            return jjStartNfaWithStates_0(4, 41, 52);
         break;
      case 108:
         return jjMoveStringLiteralDfa5_0(active0, 0x800000L);
      case 109:
         if ((active0 & 0x4000000L) != 0L)
            return jjStartNfaWithStates_0(4, 26, 52);
         return jjMoveStringLiteralDfa5_0(active0, 0x80000000000L);
      case 110:
         return jjMoveStringLiteralDfa5_0(active0, 0x400000L);
      case 114:
         return jjMoveStringLiteralDfa5_0(active0, 0x50a02000000L);
      case 116:
         if ((active0 & 0x100000L) != 0L)
            return jjStartNfaWithStates_0(4, 20, 52);
         return jjMoveStringLiteralDfa5_0(active0, 0x1000000L);
      case 117:
         return jjMoveStringLiteralDfa5_0(active0, 0x200000L);
      default :
         break;
   }
   return jjStartNfa_0(3, active0, 0L);
}
private int jjMoveStringLiteralDfa5_0(long old0, long active0)
{
   if (((active0 &= old0)) == 0L)
      return jjStartNfa_0(3, old0, 0L);
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(4, active0, 0L);
      return 5;
   }
   switch(curChar)
   {
      case 100:
         return jjMoveStringLiteralDfa6_0(active0, 0x10400000L);
      case 101:
         if ((active0 & 0x200000L) != 0L)
            return jjStartNfaWithStates_0(5, 21, 52);
         else if ((active0 & 0x80000000000L) != 0L)
            return jjStartNfaWithStates_0(5, 43, 52);
         return jjMoveStringLiteralDfa6_0(active0, 0x200800000L);
      case 105:
         return jjMoveStringLiteralDfa6_0(active0, 0x10041000000L);
      case 109:
         return jjMoveStringLiteralDfa6_0(active0, 0x20000000L);
      case 110:
         if ((active0 & 0x800000000L) != 0L)
         {
            jjmatchedKind = 35;
            jjmatchedPos = 5;
         }
         return jjMoveStringLiteralDfa6_0(active0, 0x2000000L);
      case 114:
         return jjMoveStringLiteralDfa6_0(active0, 0x100000000L);
      case 116:
         if ((active0 & 0x40000000000L) != 0L)
            return jjStartNfaWithStates_0(5, 42, 52);
         break;
      default :
         break;
   }
   return jjStartNfa_0(4, active0, 0L);
}
private int jjMoveStringLiteralDfa6_0(long old0, long active0)
{
   if (((active0 &= old0)) == 0L)
      return jjStartNfa_0(4, old0, 0L);
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(5, active0, 0L);
      return 6;
   }
   switch(curChar)
   {
      case 97:
         return jjMoveStringLiteralDfa7_0(active0, 0x10000000000L);
      case 101:
         return jjMoveStringLiteralDfa7_0(active0, 0x160000000L);
      case 111:
         return jjMoveStringLiteralDfa7_0(active0, 0x1000000L);
      case 115:
         if ((active0 & 0x400000L) != 0L)
            return jjStartNfaWithStates_0(6, 22, 52);
         else if ((active0 & 0x2000000L) != 0L)
            return jjStartNfaWithStates_0(6, 25, 52);
         else if ((active0 & 0x200000000L) != 0L)
            return jjStartNfaWithStates_0(6, 33, 52);
         break;
      case 116:
         return jjMoveStringLiteralDfa7_0(active0, 0x800000L);
      case 117:
         return jjMoveStringLiteralDfa7_0(active0, 0x10000000L);
      default :
         break;
   }
   return jjStartNfa_0(5, active0, 0L);
}
private int jjMoveStringLiteralDfa7_0(long old0, long active0)
{
   if (((active0 &= old0)) == 0L)
      return jjStartNfa_0(5, old0, 0L);
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(6, active0, 0L);
      return 7;
   }
   switch(curChar)
   {
      case 101:
         if ((active0 & 0x800000L) != 0L)
            return jjStartNfaWithStates_0(7, 23, 52);
         break;
      case 110:
         if ((active0 & 0x1000000L) != 0L)
            return jjStartNfaWithStates_0(7, 24, 52);
         return jjMoveStringLiteralDfa8_0(active0, 0x10020000000L);
      case 114:
         return jjMoveStringLiteralDfa8_0(active0, 0x10000000L);
      case 115:
         if ((active0 & 0x40000000L) != 0L)
            return jjStartNfaWithStates_0(7, 30, 52);
         else if ((active0 & 0x100000000L) != 0L)
            return jjStartNfaWithStates_0(7, 32, 52);
         break;
      default :
         break;
   }
   return jjStartNfa_0(6, active0, 0L);
}
private int jjMoveStringLiteralDfa8_0(long old0, long active0)
{
   if (((active0 &= old0)) == 0L)
      return jjStartNfa_0(6, old0, 0L);
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(7, active0, 0L);
      return 8;
   }
   switch(curChar)
   {
      case 101:
         if ((active0 & 0x10000000L) != 0L)
            return jjStartNfaWithStates_0(8, 28, 52);
         break;
      case 116:
         if ((active0 & 0x10000000000L) != 0L)
            return jjStartNfaWithStates_0(8, 40, 52);
         return jjMoveStringLiteralDfa9_0(active0, 0x20000000L);
      default :
         break;
   }
   return jjStartNfa_0(7, active0, 0L);
}
private int jjMoveStringLiteralDfa9_0(long old0, long active0)
{
   if (((active0 &= old0)) == 0L)
      return jjStartNfa_0(7, old0, 0L);
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(8, active0, 0L);
      return 9;
   }
   switch(curChar)
   {
      case 97:
         return jjMoveStringLiteralDfa10_0(active0, 0x20000000L);
      default :
         break;
   }
   return jjStartNfa_0(8, active0, 0L);
}
private int jjMoveStringLiteralDfa10_0(long old0, long active0)
{
   if (((active0 &= old0)) == 0L)
      return jjStartNfa_0(8, old0, 0L);
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(9, active0, 0L);
      return 10;
   }
   switch(curChar)
   {
      case 116:
         return jjMoveStringLiteralDfa11_0(active0, 0x20000000L);
      default :
         break;
   }
   return jjStartNfa_0(9, active0, 0L);
}
private int jjMoveStringLiteralDfa11_0(long old0, long active0)
{
   if (((active0 &= old0)) == 0L)
      return jjStartNfa_0(9, old0, 0L);
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(10, active0, 0L);
      return 11;
   }
   switch(curChar)
   {
      case 105:
         return jjMoveStringLiteralDfa12_0(active0, 0x20000000L);
      default :
         break;
   }
   return jjStartNfa_0(10, active0, 0L);
}
private int jjMoveStringLiteralDfa12_0(long old0, long active0)
{
   if (((active0 &= old0)) == 0L)
      return jjStartNfa_0(10, old0, 0L);
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(11, active0, 0L);
      return 12;
   }
   switch(curChar)
   {
      case 111:
         return jjMoveStringLiteralDfa13_0(active0, 0x20000000L);
      default :
         break;
   }
   return jjStartNfa_0(11, active0, 0L);
}
private int jjMoveStringLiteralDfa13_0(long old0, long active0)
{
   if (((active0 &= old0)) == 0L)
      return jjStartNfa_0(11, old0, 0L);
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(12, active0, 0L);
      return 13;
   }
   switch(curChar)
   {
      case 110:
         if ((active0 & 0x20000000L) != 0L)
            return jjStartNfaWithStates_0(13, 29, 52);
         break;
      default :
         break;
   }
   return jjStartNfa_0(12, active0, 0L);
}
private int jjStartNfaWithStates_0(int pos, int kind, int state)
{
   jjmatchedKind = kind;
   jjmatchedPos = pos;
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) { return pos + 1; }
   return jjMoveNfa_0(state, pos + 1);
}
static final long[] jjbitVec0 = {
   0xfffffffffffffffeL, 0xffffffffffffffffL, 0xffffffffffffffffL, 0xffffffffffffffffL
};
static final long[] jjbitVec2 = {
   0x0L, 0x0L, 0xffffffffffffffffL, 0xffffffffffffffffL
};
static final long[] jjbitVec3 = {
   0x0L, 0x0L, 0x0L, 0x100000L
};
static final long[] jjbitVec4 = {
   0x0L, 0x0L, 0x0L, 0x40000L
};
static final long[] jjbitVec5 = {
   0x0L, 0x0L, 0x0L, 0x10000L
};
static final long[] jjbitVec6 = {
   0x8000000000L, 0x0L, 0x0L, 0x0L
};
static final long[] jjbitVec7 = {
   0x10000000000L, 0x0L, 0x0L, 0x0L
};
static final long[] jjbitVec8 = {
   0x0L, 0x0L, 0x100000000000L, 0x0L
};
static final long[] jjbitVec9 = {
   0x0L, 0x100000000L, 0x0L, 0x0L
};
static final long[] jjbitVec10 = {
   0x0L, 0x1000000000L, 0x0L, 0x0L
};
static final long[] jjbitVec11 = {
   0x0L, 0x2000000000L, 0x0L, 0x0L
};
static final long[] jjbitVec12 = {
   0x1L, 0x0L, 0x0L, 0x0L
};
static final long[] jjbitVec13 = {
   0x8L, 0x0L, 0x0L, 0x0L
};
static final long[] jjbitVec14 = {
   0x0L, 0x0L, 0x800000000000000L, 0x0L
};
static final long[] jjbitVec15 = {
   0x400000000L, 0x0L, 0x0L, 0x0L
};
private int jjMoveNfa_0(int startState, int curPos)
{
   int startsAt = 0;
   jjnewStateCnt = 72;
   int i = 1;
   jjstateSet[0] = startState;
   int kind = 0x7fffffff;
   for (;;)
   {
      if (++jjround == 0x7fffffff)
         ReInitRounds();
      if (curChar < 64)
      {
         long l = 1L << curChar;
         do
         {
            switch(jjstateSet[--i])
            {
               case 37:
               case 52:
                  if ((0x83ff409800000000L & l) == 0L)
                     break;
                  if (kind > 68)
                     kind = 68;
                  jjCheckNAdd(52);
                  break;
               case 36:
                  if ((0x83ff409800000000L & l) == 0L)
                     break;
                  if (kind > 68)
                     kind = 68;
                  jjCheckNAdd(52);
                  break;
               case 3:
                  if ((0x83ff409800000000L & l) == 0L)
                     break;
                  if (kind > 68)
                     kind = 68;
                  jjCheckNAdd(52);
                  break;
               case 2:
                  if ((0x3ff000000000000L & l) != 0L)
                  {
                     if (kind > 72)
                        kind = 72;
                     jjCheckNAddStates(0, 4);
                  }
                  else if ((0x8000409800000000L & l) != 0L)
                  {
                     if (kind > 68)
                        kind = 68;
                     jjCheckNAdd(52);
                  }
                  else if (curChar == 60)
                     jjAddStates(5, 7);
                  else if (curChar == 34)
                     jjCheckNAddTwoStates(54, 55);
                  else if (curChar == 58)
                     jjstateSet[jjnewStateCnt++] = 47;
                  else if (curChar == 62)
                     jjstateSet[jjnewStateCnt++] = 23;
                  else if (curChar == 33)
                     jjstateSet[jjnewStateCnt++] = 19;
                  else if (curChar == 38)
                     jjstateSet[jjnewStateCnt++] = 12;
                  else if (curChar == 61)
                     jjstateSet[jjnewStateCnt++] = 8;
                  else if (curChar == 47)
                     jjstateSet[jjnewStateCnt++] = 0;
                  if (curChar == 33)
                  {
                     if (kind > 55)
                        kind = 55;
                  }
                  break;
               case 30:
                  if ((0x83ff409800000000L & l) == 0L)
                     break;
                  if (kind > 68)
                     kind = 68;
                  jjCheckNAdd(52);
                  break;
               case 59:
                  if (curChar == 61)
                  {
                     if (kind > 57)
                        kind = 57;
                  }
                  if (curChar == 61)
                     jjstateSet[jjnewStateCnt++] = 60;
                  if (curChar == 61)
                     jjstateSet[jjnewStateCnt++] = 58;
                  break;
               case 0:
                  if (curChar != 47)
                     break;
                  if (kind > 10)
                     kind = 10;
                  jjCheckNAdd(1);
                  break;
               case 1:
                  if ((0xffffffffffffdbffL & l) == 0L)
                     break;
                  if (kind > 10)
                     kind = 10;
                  jjCheckNAdd(1);
                  break;
               case 4:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 19)
                     kind = 19;
                  jjstateSet[jjnewStateCnt++] = 4;
                  break;
               case 7:
                  if (curChar == 62 && kind > 51)
                     kind = 51;
                  break;
               case 8:
                  if (curChar == 61)
                     jjstateSet[jjnewStateCnt++] = 7;
                  break;
               case 9:
                  if (curChar == 61)
                     jjstateSet[jjnewStateCnt++] = 8;
                  break;
               case 12:
                  if (curChar == 38 && kind > 53)
                     kind = 53;
                  break;
               case 13:
                  if (curChar == 38)
                     jjstateSet[jjnewStateCnt++] = 12;
                  break;
               case 18:
                  if (curChar == 33 && kind > 55)
                     kind = 55;
                  break;
               case 19:
                  if (curChar == 61 && kind > 56)
                     kind = 56;
                  break;
               case 20:
                  if (curChar == 33)
                     jjstateSet[jjnewStateCnt++] = 19;
                  break;
               case 23:
                  if (curChar == 61 && kind > 58)
                     kind = 58;
                  break;
               case 24:
                  if (curChar == 62)
                     jjstateSet[jjnewStateCnt++] = 23;
                  break;
               case 47:
                  if (curChar == 58 && kind > 63)
                     kind = 63;
                  break;
               case 48:
                  if (curChar == 58)
                     jjstateSet[jjnewStateCnt++] = 47;
                  break;
               case 51:
                  if ((0x8000409800000000L & l) == 0L)
                     break;
                  if (kind > 68)
                     kind = 68;
                  jjCheckNAdd(52);
                  break;
               case 53:
                  if (curChar == 34)
                     jjCheckNAddTwoStates(54, 55);
                  break;
               case 54:
                  if ((0xfffffffbffffdbffL & l) != 0L)
                     jjCheckNAddTwoStates(54, 55);
                  break;
               case 55:
                  if (curChar == 34 && kind > 70)
                     kind = 70;
                  break;
               case 56:
                  if (curChar == 60)
                     jjAddStates(5, 7);
                  break;
               case 57:
                  if (curChar == 62 && kind > 50)
                     kind = 50;
                  break;
               case 58:
                  if (curChar == 61)
                     jjstateSet[jjnewStateCnt++] = 57;
                  break;
               case 60:
                  if (curChar == 61 && kind > 52)
                     kind = 52;
                  break;
               case 61:
                  if (curChar == 61)
                     jjstateSet[jjnewStateCnt++] = 60;
                  break;
               case 62:
                  if (curChar == 61 && kind > 57)
                     kind = 57;
                  break;
               case 63:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 72)
                     kind = 72;
                  jjCheckNAddStates(0, 4);
                  break;
               case 64:
                  if ((0x3ff000000000000L & l) != 0L)
                     jjCheckNAddTwoStates(64, 67);
                  break;
               case 66:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 69)
                     kind = 69;
                  jjstateSet[jjnewStateCnt++] = 66;
                  break;
               case 68:
                  if ((0x3ff000000000000L & l) != 0L)
                     jjCheckNAddTwoStates(68, 69);
                  break;
               case 69:
                  if (curChar == 46)
                     jjCheckNAdd(70);
                  break;
               case 70:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 71)
                     kind = 71;
                  jjCheckNAdd(70);
                  break;
               case 71:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 72)
                     kind = 72;
                  jjCheckNAdd(71);
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      else if (curChar < 128)
      {
         long l = 1L << (curChar & 077);
         do
         {
            switch(jjstateSet[--i])
            {
               case 37:
                  if ((0x47ffffffd7fffffeL & l) != 0L)
                  {
                     if (kind > 68)
                        kind = 68;
                     jjCheckNAdd(52);
                  }
                  if (curChar == 120)
                     jjstateSet[jjnewStateCnt++] = 36;
                  break;
               case 36:
                  if ((0x47ffffffd7fffffeL & l) != 0L)
                  {
                     if (kind > 68)
                        kind = 68;
                     jjCheckNAdd(52);
                  }
                  if (curChar == 105)
                     jjstateSet[jjnewStateCnt++] = 35;
                  break;
               case 3:
                  if ((0x47ffffffd7fffffeL & l) != 0L)
                  {
                     if (kind > 68)
                        kind = 68;
                     jjCheckNAdd(52);
                  }
                  if (curChar == 118)
                     jjstateSet[jjnewStateCnt++] = 4;
                  break;
               case 2:
                  if ((0x47ffffffd7fffffeL & l) != 0L)
                  {
                     if (kind > 68)
                        kind = 68;
                     jjCheckNAdd(52);
                  }
                  else if (curChar == 124)
                     jjstateSet[jjnewStateCnt++] = 15;
                  if (curChar == 92)
                     jjstateSet[jjnewStateCnt++] = 51;
                  else if (curChar == 108)
                     jjstateSet[jjnewStateCnt++] = 44;
                  else if (curChar == 101)
                     jjstateSet[jjnewStateCnt++] = 37;
                  else if (curChar == 102)
                     jjstateSet[jjnewStateCnt++] = 30;
                  else if (curChar == 98)
                     jjstateSet[jjnewStateCnt++] = 3;
                  break;
               case 30:
                  if ((0x47ffffffd7fffffeL & l) != 0L)
                  {
                     if (kind > 68)
                        kind = 68;
                     jjCheckNAdd(52);
                  }
                  if (curChar == 111)
                     jjstateSet[jjnewStateCnt++] = 29;
                  break;
               case 1:
                  if (kind > 10)
                     kind = 10;
                  jjstateSet[jjnewStateCnt++] = 1;
                  break;
               case 5:
                  if (curChar == 98)
                     jjstateSet[jjnewStateCnt++] = 3;
                  break;
               case 15:
                  if (curChar == 124 && kind > 54)
                     kind = 54;
                  break;
               case 16:
                  if (curChar == 124)
                     jjstateSet[jjnewStateCnt++] = 15;
                  break;
               case 26:
                  if (curChar == 108 && kind > 60)
                     kind = 60;
                  break;
               case 27:
                  if (curChar == 108)
                     jjstateSet[jjnewStateCnt++] = 26;
                  break;
               case 28:
                  if (curChar == 97)
                     jjstateSet[jjnewStateCnt++] = 27;
                  break;
               case 29:
                  if (curChar == 114)
                     jjstateSet[jjnewStateCnt++] = 28;
                  break;
               case 31:
                  if (curChar == 102)
                     jjstateSet[jjnewStateCnt++] = 30;
                  break;
               case 33:
                  if (curChar == 115 && kind > 61)
                     kind = 61;
                  break;
               case 34:
                  if (curChar == 116)
                     jjstateSet[jjnewStateCnt++] = 33;
                  break;
               case 35:
                  if (curChar == 115)
                     jjstateSet[jjnewStateCnt++] = 34;
                  break;
               case 38:
                  if (curChar == 101)
                     jjstateSet[jjnewStateCnt++] = 37;
                  break;
               case 40:
                  if (curChar == 97 && kind > 62)
                     kind = 62;
                  break;
               case 41:
                  if (curChar == 100)
                     jjstateSet[jjnewStateCnt++] = 40;
                  break;
               case 42:
                  if (curChar == 98)
                     jjstateSet[jjnewStateCnt++] = 41;
                  break;
               case 43:
                  if (curChar == 109)
                     jjstateSet[jjnewStateCnt++] = 42;
                  break;
               case 44:
                  if (curChar == 97)
                     jjstateSet[jjnewStateCnt++] = 43;
                  break;
               case 45:
                  if (curChar == 108)
                     jjstateSet[jjnewStateCnt++] = 44;
                  break;
               case 50:
                  if (curChar == 92)
                     jjstateSet[jjnewStateCnt++] = 51;
                  break;
               case 51:
                  if ((0x47ffffffd7fffffeL & l) == 0L)
                     break;
                  if (kind > 68)
                     kind = 68;
                  jjCheckNAdd(52);
                  break;
               case 52:
                  if ((0x47ffffffd7fffffeL & l) == 0L)
                     break;
                  if (kind > 68)
                     kind = 68;
                  jjCheckNAdd(52);
                  break;
               case 54:
                  if ((0xffffffffefffffffL & l) != 0L)
                     jjAddStates(8, 9);
                  break;
               case 65:
                  if (curChar == 118)
                     jjstateSet[jjnewStateCnt++] = 66;
                  break;
               case 67:
                  if (curChar == 98)
                     jjstateSet[jjnewStateCnt++] = 65;
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      else
      {
         int hiByte = (int)(curChar >> 8);
         int i1 = hiByte >> 6;
         long l1 = 1L << (hiByte & 077);
         int i2 = (curChar & 0xff) >> 6;
         long l2 = 1L << (curChar & 077);
         do
         {
            switch(jjstateSet[--i])
            {
               case 2:
                  if (jjCanMove_1(hiByte, i1, i2, l1, l2))
                  {
                     if (kind > 50)
                        kind = 50;
                  }
                  if (jjCanMove_2(hiByte, i1, i2, l1, l2))
                  {
                     if (kind > 51)
                        kind = 51;
                  }
                  if (jjCanMove_3(hiByte, i1, i2, l1, l2))
                  {
                     if (kind > 52)
                        kind = 52;
                  }
                  if (jjCanMove_4(hiByte, i1, i2, l1, l2))
                  {
                     if (kind > 53)
                        kind = 53;
                  }
                  if (jjCanMove_5(hiByte, i1, i2, l1, l2))
                  {
                     if (kind > 54)
                        kind = 54;
                  }
                  if (jjCanMove_6(hiByte, i1, i2, l1, l2))
                  {
                     if (kind > 55)
                        kind = 55;
                  }
                  if (jjCanMove_7(hiByte, i1, i2, l1, l2))
                  {
                     if (kind > 56)
                        kind = 56;
                  }
                  if (jjCanMove_8(hiByte, i1, i2, l1, l2))
                  {
                     if (kind > 57)
                        kind = 57;
                  }
                  if (jjCanMove_9(hiByte, i1, i2, l1, l2))
                  {
                     if (kind > 58)
                        kind = 58;
                  }
                  if (jjCanMove_10(hiByte, i1, i2, l1, l2))
                  {
                     if (kind > 60)
                        kind = 60;
                  }
                  if (jjCanMove_11(hiByte, i1, i2, l1, l2))
                  {
                     if (kind > 61)
                        kind = 61;
                  }
                  if (jjCanMove_12(hiByte, i1, i2, l1, l2))
                  {
                     if (kind > 62)
                        kind = 62;
                  }
                  if (jjCanMove_13(hiByte, i1, i2, l1, l2))
                  {
                     if (kind > 63)
                        kind = 63;
                  }
                  break;
               case 1:
                  if (!jjCanMove_0(hiByte, i1, i2, l1, l2))
                     break;
                  if (kind > 10)
                     kind = 10;
                  jjstateSet[jjnewStateCnt++] = 1;
                  break;
               case 6:
                  if (jjCanMove_1(hiByte, i1, i2, l1, l2) && kind > 50)
                     kind = 50;
                  break;
               case 10:
                  if (jjCanMove_2(hiByte, i1, i2, l1, l2) && kind > 51)
                     kind = 51;
                  break;
               case 11:
                  if (jjCanMove_3(hiByte, i1, i2, l1, l2) && kind > 52)
                     kind = 52;
                  break;
               case 14:
                  if (jjCanMove_4(hiByte, i1, i2, l1, l2) && kind > 53)
                     kind = 53;
                  break;
               case 17:
                  if (jjCanMove_5(hiByte, i1, i2, l1, l2) && kind > 54)
                     kind = 54;
                  break;
               case 18:
                  if (jjCanMove_6(hiByte, i1, i2, l1, l2) && kind > 55)
                     kind = 55;
                  break;
               case 21:
                  if (jjCanMove_7(hiByte, i1, i2, l1, l2) && kind > 56)
                     kind = 56;
                  break;
               case 22:
                  if (jjCanMove_8(hiByte, i1, i2, l1, l2) && kind > 57)
                     kind = 57;
                  break;
               case 25:
                  if (jjCanMove_9(hiByte, i1, i2, l1, l2) && kind > 58)
                     kind = 58;
                  break;
               case 32:
                  if (jjCanMove_10(hiByte, i1, i2, l1, l2) && kind > 60)
                     kind = 60;
                  break;
               case 39:
                  if (jjCanMove_11(hiByte, i1, i2, l1, l2) && kind > 61)
                     kind = 61;
                  break;
               case 46:
                  if (jjCanMove_12(hiByte, i1, i2, l1, l2) && kind > 62)
                     kind = 62;
                  break;
               case 49:
                  if (jjCanMove_13(hiByte, i1, i2, l1, l2) && kind > 63)
                     kind = 63;
                  break;
               case 54:
                  if (jjCanMove_0(hiByte, i1, i2, l1, l2))
                     jjAddStates(8, 9);
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      if (kind != 0x7fffffff)
      {
         jjmatchedKind = kind;
         jjmatchedPos = curPos;
         kind = 0x7fffffff;
      }
      ++curPos;
      if ((i = jjnewStateCnt) == (startsAt = 72 - (jjnewStateCnt = startsAt)))
         return curPos;
      try { curChar = input_stream.readChar(); }
      catch(java.io.IOException e) { return curPos; }
   }
}
private int jjMoveStringLiteralDfa0_1()
{
   switch(curChar)
   {
      case 42:
         return jjMoveStringLiteralDfa1_1(0x2000L);
      case 47:
         return jjMoveStringLiteralDfa1_1(0x1000L);
      default :
         return 1;
   }
}
private int jjMoveStringLiteralDfa1_1(long active0)
{
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      return 1;
   }
   switch(curChar)
   {
      case 42:
         if ((active0 & 0x1000L) != 0L)
            return jjStopAtPos(1, 12);
         break;
      case 47:
         if ((active0 & 0x2000L) != 0L)
            return jjStopAtPos(1, 13);
         break;
      default :
         return 2;
   }
   return 2;
}
static final int[] jjnextStates = {
   64, 67, 68, 69, 71, 59, 61, 62, 54, 55, 
};
private static final boolean jjCanMove_0(int hiByte, int i1, int i2, long l1, long l2)
{
   switch(hiByte)
   {
      case 0:
         return ((jjbitVec2[i2] & l2) != 0L);
      default :
         if ((jjbitVec0[i1] & l1) != 0L)
            return true;
         return false;
   }
}
private static final boolean jjCanMove_1(int hiByte, int i1, int i2, long l1, long l2)
{
   switch(hiByte)
   {
      case 33:
         return ((jjbitVec3[i2] & l2) != 0L);
      default :
         return false;
   }
}
private static final boolean jjCanMove_2(int hiByte, int i1, int i2, long l1, long l2)
{
   switch(hiByte)
   {
      case 33:
         return ((jjbitVec4[i2] & l2) != 0L);
      default :
         return false;
   }
}
private static final boolean jjCanMove_3(int hiByte, int i1, int i2, long l1, long l2)
{
   switch(hiByte)
   {
      case 33:
         return ((jjbitVec5[i2] & l2) != 0L);
      default :
         return false;
   }
}
private static final boolean jjCanMove_4(int hiByte, int i1, int i2, long l1, long l2)
{
   switch(hiByte)
   {
      case 34:
         return ((jjbitVec6[i2] & l2) != 0L);
      default :
         return false;
   }
}
private static final boolean jjCanMove_5(int hiByte, int i1, int i2, long l1, long l2)
{
   switch(hiByte)
   {
      case 34:
         return ((jjbitVec7[i2] & l2) != 0L);
      default :
         return false;
   }
}
private static final boolean jjCanMove_6(int hiByte, int i1, int i2, long l1, long l2)
{
   switch(hiByte)
   {
      case 0:
         return ((jjbitVec8[i2] & l2) != 0L);
      default :
         return false;
   }
}
private static final boolean jjCanMove_7(int hiByte, int i1, int i2, long l1, long l2)
{
   switch(hiByte)
   {
      case 34:
         return ((jjbitVec9[i2] & l2) != 0L);
      default :
         return false;
   }
}
private static final boolean jjCanMove_8(int hiByte, int i1, int i2, long l1, long l2)
{
   switch(hiByte)
   {
      case 34:
         return ((jjbitVec10[i2] & l2) != 0L);
      default :
         return false;
   }
}
private static final boolean jjCanMove_9(int hiByte, int i1, int i2, long l1, long l2)
{
   switch(hiByte)
   {
      case 34:
         return ((jjbitVec11[i2] & l2) != 0L);
      default :
         return false;
   }
}
private static final boolean jjCanMove_10(int hiByte, int i1, int i2, long l1, long l2)
{
   switch(hiByte)
   {
      case 34:
         return ((jjbitVec12[i2] & l2) != 0L);
      default :
         return false;
   }
}
private static final boolean jjCanMove_11(int hiByte, int i1, int i2, long l1, long l2)
{
   switch(hiByte)
   {
      case 34:
         return ((jjbitVec13[i2] & l2) != 0L);
      default :
         return false;
   }
}
private static final boolean jjCanMove_12(int hiByte, int i1, int i2, long l1, long l2)
{
   switch(hiByte)
   {
      case 3:
         return ((jjbitVec14[i2] & l2) != 0L);
      default :
         return false;
   }
}
private static final boolean jjCanMove_13(int hiByte, int i1, int i2, long l1, long l2)
{
   switch(hiByte)
   {
      case 32:
         return ((jjbitVec15[i2] & l2) != 0L);
      default :
         return false;
   }
}

/** Token literal values. */
public static final String[] jjstrLiteralImages = {
"", null, null, null, null, null, null, null, null, null, null, null, null, 
null, null, "\166\141\162", "\167\150\145\162\145", "\151\156\164", 
"\142\157\157\154", null, "\143\157\156\163\164", "\165\156\151\161\165\145", 
"\145\170\164\145\156\144\163", "\143\157\155\160\154\145\164\145", "\146\165\156\143\164\151\157\156", 
"\162\145\164\165\162\156\163", "\141\170\151\157\155", "\164\171\160\145", 
"\160\162\157\143\145\144\165\162\145", "\151\155\160\154\145\155\145\156\164\141\164\151\157\156", 
"\155\157\144\151\146\151\145\163", "\146\162\145\145", "\162\145\161\165\151\162\145\163", 
"\145\156\163\165\162\145\163", "\147\157\164\157", "\162\145\164\165\162\156", "\151\146", 
"\164\150\145\156", "\145\154\163\145", "\167\150\151\154\145", 
"\151\156\166\141\162\151\141\156\164", "\142\162\145\141\153", "\141\163\163\145\162\164", 
"\141\163\163\165\155\145", "\150\141\166\157\143", "\143\141\154\154", "\146\141\154\163\145", 
"\164\162\165\145", "\157\154\144", "\72\75", null, null, null, null, null, null, null, null, null, 
"\52", null, null, null, null, "\175", "\173", "\135", "\133", null, null, null, null, 
null, "\73", "\50", "\51", "\72", "\54", "\74", "\76", "\75", "\75\75", "\74\72", 
"\53\53", "\53", "\55", "\57", "\45", "\174\173", "\175\174", };

/** Lexer state names. */
public static final String[] lexStateNames = {
   "DEFAULT",
   "COMMENT",
};

/** Lex State array. */
public static final int[] jjnewLexState = {
   -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 
   -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 
   -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 
   -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 
};
static final long[] jjtoToken = {
   0xffffffffffff8001L, 0x3ffffffL, 
};
static final long[] jjtoSkip = {
   0x7fc0L, 0x0L, 
};
protected JavaCharStream input_stream;
private final int[] jjrounds = new int[72];
private final int[] jjstateSet = new int[144];
private final StringBuilder jjimage = new StringBuilder();
private StringBuilder image = jjimage;
private int jjimageLen;
private int lengthOfMatch;
protected char curChar;
/** Constructor. */
public BPLParserTokenManager(JavaCharStream stream){
   if (JavaCharStream.staticFlag)
      throw new Error("ERROR: Cannot use a static CharStream class with a non-static lexical analyzer.");
   input_stream = stream;
}

/** Constructor. */
public BPLParserTokenManager(JavaCharStream stream, int lexState){
   this(stream);
   SwitchTo(lexState);
}

/** Reinitialise parser. */
public void ReInit(JavaCharStream stream)
{
   jjmatchedPos = jjnewStateCnt = 0;
   curLexState = defaultLexState;
   input_stream = stream;
   ReInitRounds();
}
private void ReInitRounds()
{
   int i;
   jjround = 0x80000001;
   for (i = 72; i-- > 0;)
      jjrounds[i] = 0x80000000;
}

/** Reinitialise parser. */
public void ReInit(JavaCharStream stream, int lexState)
{
   ReInit(stream);
   SwitchTo(lexState);
}

/** Switch to specified lex state. */
public void SwitchTo(int lexState)
{
   if (lexState >= 2 || lexState < 0)
      throw new TokenMgrError("Error: Ignoring invalid lexical state : " + lexState + ". State unchanged.", TokenMgrError.INVALID_LEXICAL_STATE);
   else
      curLexState = lexState;
}

protected Token jjFillToken()
{
   final Token t;
   final String curTokenImage;
   final int beginLine;
   final int endLine;
   final int beginColumn;
   final int endColumn;
   String im = jjstrLiteralImages[jjmatchedKind];
   curTokenImage = (im == null) ? input_stream.GetImage() : im;
   beginLine = input_stream.getBeginLine();
   beginColumn = input_stream.getBeginColumn();
   endLine = input_stream.getEndLine();
   endColumn = input_stream.getEndColumn();
   t = Token.newToken(jjmatchedKind, curTokenImage);

   t.beginLine = beginLine;
   t.endLine = endLine;
   t.beginColumn = beginColumn;
   t.endColumn = endColumn;

   return t;
}

int curLexState = 0;
int defaultLexState = 0;
int jjnewStateCnt;
int jjround;
int jjmatchedPos;
int jjmatchedKind;

/** Get the next Token. */
public Token getNextToken() 
{
  Token matchedToken;
  int curPos = 0;

  EOFLoop :
  for (;;)
  {
   try
   {
      curChar = input_stream.BeginToken();
   }
   catch(java.io.IOException e)
   {
      jjmatchedKind = 0;
      matchedToken = jjFillToken();
      return matchedToken;
   }
   image = jjimage;
   image.setLength(0);
   jjimageLen = 0;

   switch(curLexState)
   {
     case 0:
       try { input_stream.backup(0);
          while (curChar <= 32 && (0x100002600L & (1L << curChar)) != 0L)
             curChar = input_stream.BeginToken();
       }
       catch (java.io.IOException e1) { continue EOFLoop; }
       jjmatchedKind = 0x7fffffff;
       jjmatchedPos = 0;
       curPos = jjMoveStringLiteralDfa0_0();
       break;
     case 1:
       jjmatchedKind = 0x7fffffff;
       jjmatchedPos = 0;
       curPos = jjMoveStringLiteralDfa0_1();
       if (jjmatchedPos == 0 && jjmatchedKind > 14)
       {
          jjmatchedKind = 14;
       }
       break;
   }
     if (jjmatchedKind != 0x7fffffff)
     {
        if (jjmatchedPos + 1 < curPos)
           input_stream.backup(curPos - jjmatchedPos - 1);
        if ((jjtoToken[jjmatchedKind >> 6] & (1L << (jjmatchedKind & 077))) != 0L)
        {
           matchedToken = jjFillToken();
       if (jjnewLexState[jjmatchedKind] != -1)
         curLexState = jjnewLexState[jjmatchedKind];
           return matchedToken;
        }
        else
        {
           SkipLexicalActions(null);
         if (jjnewLexState[jjmatchedKind] != -1)
           curLexState = jjnewLexState[jjmatchedKind];
           continue EOFLoop;
        }
     }
     int error_line = input_stream.getEndLine();
     int error_column = input_stream.getEndColumn();
     String error_after = null;
     boolean EOFSeen = false;
     try { input_stream.readChar(); input_stream.backup(1); }
     catch (java.io.IOException e1) {
        EOFSeen = true;
        error_after = curPos <= 1 ? "" : input_stream.GetImage();
        if (curChar == '\n' || curChar == '\r') {
           error_line++;
           error_column = 0;
        }
        else
           error_column++;
     }
     if (!EOFSeen) {
        input_stream.backup(1);
        error_after = curPos <= 1 ? "" : input_stream.GetImage();
     }
     throw new TokenMgrError(EOFSeen, curLexState, error_line, error_column, error_after, curChar, TokenMgrError.LEXICAL_ERROR);
  }
}

void SkipLexicalActions(Token matchedToken)
{
   switch(jjmatchedKind)
   {
      case 11 :
         image.append(input_stream.GetSuffix(jjimageLen + (lengthOfMatch = jjmatchedPos + 1)));
         commentNestingDepth = 1 ;
         break;
      case 12 :
         image.append(input_stream.GetSuffix(jjimageLen + (lengthOfMatch = jjmatchedPos + 1)));
         commentNestingDepth ++ ;
         break;
      case 13 :
         image.append(input_stream.GetSuffix(jjimageLen + (lengthOfMatch = jjmatchedPos + 1)));
         commentNestingDepth --;
         SwitchTo( commentNestingDepth==0 ? DEFAULT : COMMENT ) ;
         break;
      default :
         break;
   }
}
private void jjCheckNAdd(int state)
{
   if (jjrounds[state] != jjround)
   {
      jjstateSet[jjnewStateCnt++] = state;
      jjrounds[state] = jjround;
   }
}
private void jjAddStates(int start, int end)
{
   do {
      jjstateSet[jjnewStateCnt++] = jjnextStates[start];
   } while (start++ != end);
}
private void jjCheckNAddTwoStates(int state1, int state2)
{
   jjCheckNAdd(state1);
   jjCheckNAdd(state2);
}

private void jjCheckNAddStates(int start, int end)
{
   do {
      jjCheckNAdd(jjnextStates[start]);
   } while (start++ != end);
}

}
