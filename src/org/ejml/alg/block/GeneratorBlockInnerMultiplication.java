/*
 * Copyright (c) 2009-2012, Peter Abeles. All Rights Reserved.
 *
 * This file is part of Efficient Java Matrix Library (EJML).
 *
 * EJML is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * EJML is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EJML.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.ejml.alg.block;

import org.ejml.alg.generic.CodeGeneratorMisc;

import java.io.FileNotFoundException;
import java.io.PrintStream;


/**
 * @author Peter Abeles
 */
public class GeneratorBlockInnerMultiplication {

    String className;
    PrintStream stream;

    public GeneratorBlockInnerMultiplication( String className ) throws FileNotFoundException {
        this.className = className;
        stream = new PrintStream(className+".java");
    }

    public void createClass() {
        printTop();

        for( int i = 0; i < 2; i++ ) {
            boolean hasAlpha = i==1;
            for( Operation o : Operation.values()) {
                if( hasAlpha && o == Operation.MINUS )
                    continue;
                print_mult(hasAlpha,o);
                print_multTransA(hasAlpha,o);
                print_multTransB(hasAlpha,o);
            }
        }


        stream.print("}\n");
    }

    private void printTop() {
        String foo = CodeGeneratorMisc.COPYRIGHT +
                "\n" +
                "package org.ejml.alg.block;\n" +
                "\n" +
                "/**\n" +
                " * <p>\n" +
                " * Matrix multiplication for the inner row major blocks, typically inside of a {@link org.ejml.data.BlockMatrix64F}.\n" +
                " * </p>\n" +
                " *\n" +
                " * <p>\n" +
                " * This code was auto generated by {@link GeneratorBlockInnerMultiplication} and should not be modified directly.\n" +
                " * </p>\n" +
                " *\n" +
                " * @author Peter Abeles\n" +
                " */\n" +
                "public class "+className+" {\n";

        stream.print(foo);
    }

    private void print_mult( boolean hasAlpha , Operation opType ) {

        createHeader(hasAlpha,opType,false,false);

        stream.print(
                "//        for( int i = 0; i < heightA; i++ ) {\n" +
                "//            for( int k = 0; k < widthA; k++ ) {\n" +
                "//                for( int j = 0; j < widthC; j++ ) {\n" +
                "//                    dataC[ i*widthC + j + indexC ] += dataA[i*widthA + k + indexA] * dataB[k*widthC + j + indexB];\n" +
                "//                }\n" +
                "//            }\n" +
                "//        }\n");

        stream.println();

        String o = ( opType == Operation.MINUS ) ? "-=" : "+=";
        String m = hasAlpha ? "alpha*" : "";

        stream.print(
                "        int a = indexA;\n"+
                "        int rowC = indexC;\n"+
                "        for( int i = 0; i < heightA; i++ , rowC += widthC ) {\n" +
                "            int b = indexB;\n" +
                "\n" +
                "            final int endC = rowC + widthC;\n" +
                "            final int endA = a + widthA;"+
                "\n"+
                "            while( a != endA ) {//for( int k = 0; k < widthA; k++ ) {\n" +
                "                double valA = "+m+"dataA[a++];\n" +
                "\n" +
                "                int c = rowC;\n" +
                "\n");

        if( opType == Operation.SET ) {
             stream.print(
                "                if( b == indexB ) {\n" +
                "                    while( c != endC  ) {//for( int j = 0; j < widthC; j++ ) {\n" +
                "                        dataC[ c++ ] = valA * dataB[ b++ ];\n" +
                "                    }\n" +
                "                } else {\n" +
                "                    while( c != endC  ) {//for( int j = 0; j < widthC; j++ ) {\n" +
                "                        dataC[ c++ ] "+o+" valA * dataB[ b++ ];\n" +
                "                    }\n" +
                "                }\n");
        } else {
             stream.print(
                "                while( c != endC  ) {//for( int j = 0; j < widthC; j++ ) {\n" +
                "                    dataC[ c++ ] "+o+" valA * dataB[ b++ ];\n" +
                "                }\n");
        }
        stream.println(
                "            }\n" +
                "        }");

        stream.println("    }");

    }
    

    private String createOpString(boolean hasAlpha, Operation opType) {
        String o = opString(opType);
        if( hasAlpha ) o += " alpha * ";
        return o;
    }

    private void print_multTransA( boolean hasAlpha , Operation opType ) {

        createHeader(hasAlpha,opType,true,false);

        String o = ( opType == Operation.MINUS ) ? "-=" : "+=";
        String m = hasAlpha ? "alpha*" : "";

        stream.print(
                "//        for( int i = 0; i < widthA; i++ ) {\n" +
                "//            for( int k = 0; k < heightA; k++ ) {\n" +
                "//                double valA = dataA[k*widthA + i + indexA];\n" +
                "//                for( int j = 0; j < widthC; j++ ) {\n" +
                "//                    dataC[ i*widthC + j + indexC ] += valA * dataB[k*widthC + j + indexB];\n" +
                "//                }\n" +
                "//            }\n" +
                "//        }\n");
        stream.println();

        stream.print(
        "        int rowC = indexC;\n"+
        "        for( int i = 0; i < widthA; i++ , rowC += widthC) {\n" +
        "            int colA = i + indexA;\n" +
        "            int endA = colA + widthA*heightA;\n" +
        "            int b = indexB;\n" +
        "\n" +
        "            // for( int k = 0; k < heightA; k++ ) {\n" +
        "            while(colA != endA ) {\n" +
        "                double valA = "+m+"dataA[colA];\n" +
        "\n" +
        "                int c = rowC;\n" +
        "                final int endB = b + widthC;\n" +
        "\n" +
        "                //for( int j = 0; j < widthC; j++ ) {\n");
        if( opType == Operation.SET ) {
            stream.print(
                    "                if( b == indexB ) {\n" +
                    "                    while( b != endB ) {\n" +
                    "                        dataC[ c++ ] = valA * dataB[b++];\n" +
                    "                    } \n" +
                    "                } else {\n" +
                    "                    while( b != endB ) {\n" +
                    "                        dataC[ c++ ] "+o+" valA * dataB[b++];\n" +
                    "                    }\n" +
                    "                }\n");
        } else {
            stream.print(
                    "                while( b != endB ) {\n" +
                    "                    dataC[ c++ ] "+o+" valA * dataB[b++];\n" +
                    "                }\n");
        }
        stream.print(
        "                colA += widthA;\n"+
        "            }\n" +
        "        }\n");


        stream.println("    }");
    }

    private void print_multTransB( boolean hasAlpha , Operation opType ) {

        createHeader(hasAlpha,opType,false,true);

        String o = createOpString(hasAlpha, opType);

        stream.println(
                "        for( int i = 0; i < heightA; i++ ) {\n" +
                "            for( int j = 0; j < widthC; j++ ) {\n" +
                "                double val = 0;\n" +
                "\n" +
                "                for( int k = 0; k < widthA; k++ ) {\n" +
                "                    val += dataA[i*widthA + k + indexA] * dataB[j*widthA + k + indexB];\n" +
                "                }\n" +
                "\n" +
                "                dataC[ i*widthC + j + indexC ] "+o+" val;\n" +
                "            }\n" +
                "        }");

        stream.println("    }");
    }

    private void createHeader( boolean hasAlpha , Operation opType , boolean transA , boolean transB )
    {
        String alphaString = hasAlpha ? " &alpha; " : "";
        String alphaParam = hasAlpha ? " double alpha ," : "";
        String transAString = transA ? "<sup>T</sup>" : "";
        String transBString = transB ? "<sup>T</sup>" : "";
        String opTypeString;

        switch( opType ) {
            case MINUS: opTypeString = "C - "; break;
            case PLUS: opTypeString = "C + "; break;
            case SET: opTypeString = ""; break;
            default: throw new RuntimeException("Unknown optype");
        }


        String funcName = "blockMult"+opName(opType);
        if( transA && transB ) funcName += "TransAB";
        else if( transA ) funcName += "TransA";
        else if( transB ) funcName += "TransB";

        stream.println();
        stream.print(
                "    /**\n" +
                "     * <p>\n" +
                "     * Performs the follow operation on individual inner blocks:<br>\n" +
                "     * <br>\n");

        stream.print(
                "     * C = "+opTypeString+alphaString+"A"+transAString+" * B"+transBString+"\n");
        stream.print(
                "     * </p>\n" +
                "     */\n" +
                "    public static void "+funcName+"("+alphaParam+" final double[] dataA, final double []dataB, final double []dataC,\n" +
                "                                     int indexA, int indexB, int indexC,\n" +
                "                                     final int heightA, final int widthA, final int widthC) {\n");
    }

    private String opString( Operation opType ) {
        switch( opType ) {
            case MINUS:
                return "-=";

            case PLUS:
                return "+=";

            case SET:
                return "=";

            default:
                throw new RuntimeException("Unknown opType "+opType);
        }
    }

    private String opName( Operation opType ) {
        switch( opType ) {
            case MINUS:
                return "Minus";

            case PLUS:
                return "Plus";

            case SET:
                return "Set";

            default:
                throw new RuntimeException("Unknown opType "+opType);
        }
    }

    private static enum Operation
    {

        /** Add results to output matrix */
        PLUS,
        /** Subtract results from output matrix */
        MINUS,
        /** set output matrix to results */
        SET
    }

    public static void main( String args[] ) throws FileNotFoundException {
        GeneratorBlockInnerMultiplication app = new GeneratorBlockInnerMultiplication("BlockInnerMultiplication");

        app.createClass();

        System.out.println("Done generating class");

    }
}
