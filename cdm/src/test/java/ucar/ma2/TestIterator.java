/*
 * Copyright 1998-2009 University Corporation for Atmospheric Research/Unidata
 *
 * Portions of this software were developed by the Unidata Program at the
 * University Corporation for Atmospheric Research.
 *
 * Access and use of this software shall impose the following obligations
 * and understandings on the user. The user is granted the right, without
 * any fee or cost, to use, copy, modify, alter, enhance and distribute
 * this software, and any derivative works thereof, and its supporting
 * documentation for any purpose whatsoever, provided that this entire
 * notice appears in all copies of the software, derivative works and
 * supporting documentation.  Further, UCAR requests that the user credit
 * UCAR/Unidata in any publications that result from the use of this
 * software or in any product that includes this software. The names UCAR
 * and/or Unidata, however, may not be used in any advertising or publicity
 * to endorse or promote any products or commercial entity unless specific
 * written permission is obtained from UCAR/Unidata. The user also
 * understands that UCAR/Unidata is not obligated to provide the user with
 * any support, consulting, training or assistance of any kind with regard
 * to the use, operation and performance of this software nor to provide
 * the user with any updates, revisions, new versions or "bug fixes."
 *
 * THIS SOFTWARE IS PROVIDED BY UCAR/UNIDATA "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL UCAR/UNIDATA BE LIABLE FOR ANY SPECIAL,
 * INDIRECT OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING
 * FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT,
 * NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION
 * WITH THE ACCESS, USE OR PERFORMANCE OF THIS SOFTWARE.
 */
package ucar.ma2;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.nc2.util.Misc;

import java.lang.invoke.MethodHandles;

/** Test ma2 section methods in the JUnit framework. */

public class TestIterator {
  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  int m = 4, n = 3, p = 2;
  int [] sA = { m, n, p };
  int stride1 = p;
  int stride2 = n * p;

  ArrayDouble A = new ArrayDouble(sA);
  Index ima;
  int i,j,k;

  ArrayDouble secA;
  int m1 = 2;
  int m2 = 3;
  int mlen = (m2 - m1 + 1);

  @Before
  public void setUp() {
    ima = A.getIndex();

    // write
    for (int i=0; i<m; i++) {
      ima.set0(i);
      for (j=0; j<n; j++) {
        ima.set1(j);
        for (k=0; k<p; k++) {
          ima.set2(k);
          A.setDouble(ima, (double) (i*100+j*10+k));
        }
      }
    }

  }

  @Test
  public void testFastIter() {

    int count = 0;
    IndexIterator iter = A.getIndexIterator();
    //System.out.println(iter);
    while(iter.hasNext()) {
      iter.getDoubleNext();
      count++;
    }
    assert(count == n*m*p);

    iter = A.getIndexIterator();
    for (i=0; i<m; i++) {
      for (j=0; j<n; j++) {
        for (k=0; k<p; k++) {
          double val = iter.getDoubleNext();
          double myVal = (double) (i*100+j*10+k);
          Assert.assertEquals(val, myVal, Misc.maxReletiveError);
        }
      }
    }
  }


  @Test
  public void testSlowIter() {

    int count = 0;
    Index index = A.getIndex();
    IndexIterator iter = index.getSlowIndexIterator(A);

    //System.out.println(iter);
    while(iter.hasNext()) {
      iter.getDoubleNext();
      count++;
    }
    assert(count == n*m*p);

    iter = A.getIndexIterator();
    for (i=0; i<m; i++) {
      for (j=0; j<n; j++) {
        for (k=0; k<p; k++) {
          double val = iter.getDoubleNext();
          double myVal = (double) (i*100+j*10+k);
          Assert.assertEquals(val, myVal, Misc.maxReletiveError);
        }
      }
    }
  }


  @Test
  public void testScalerIter() {

    ArrayDouble.D0 scalar = new ArrayDouble.D0();
    scalar.set( 10.0);

    int count = 0;
    double sum = 0;
    IndexIterator iter = scalar.getIndexIterator();
    while(iter.hasNext()) {
      sum += iter.getDoubleNext();
      count++;
    }
    assert(count == 1);
    assert(sum == 10.0);

    System.out.println(" testScalerIter");
  }

  @Test
  public void testSectionIter() {

    try {
      secA = (ArrayDouble) A.section( new Section(m1+":"+m2+",:,:").getRanges() );
    } catch (InvalidRangeException e) {
      System.out.println("testMAsection failed == "+ e);
      return;
    }

    int count = 0;
    IndexIterator iter = secA.getIndexIterator();
    //System.out.println(iter);
    while(iter.hasNext()) {
      iter.getDoubleNext();
      count++;
    }
    assert(count == n*mlen*p);

    iter = secA.getIndexIterator();
    for (i=0; i<mlen; i++) {
      for (j=0; j<n; j++) {
        for (k=0; k<p; k++) {
          double val = iter.getDoubleNext();
          double myVal = (double) (i+m1)*100+j*10+k;
          assert (val == (i+m1)*100+j*10+k);
        }
      }
    }
  }


  @Test
    public void testSectionRank2() {
      ArrayDouble secA2;
      try {
        secA2 = (ArrayDouble) A.section( new Section("2,:,:").getRanges() );
      } catch (InvalidRangeException e) {
        System.out.println("testMAsectionrank2 failed == "+ e);
        return;
      }

      IndexIterator iter = secA2.getIndexIterator();
      for (i=0; i<n; i++) {
        for (j=0; j<p; j++) {
            double val = iter.getDoubleNext();
            //double myVal = (double) (i+m1)*100+j*10+k;
            //System.out.println(val+ " "+myVal);
            assert (val == 200+i*10+j);
        }
      }
    }

  @Test
  public void testArrayObjectIter() throws InvalidRangeException {
    Array arrayObject = Array.factory(DataType.OPAQUE, new int[] {30});
    testArrayObjectIter(arrayObject, 30);

    Array arrayObjectSubset = arrayObject.sectionNoReduce(new Section("1:20").getRanges()); // subset it
    testArrayObjectIter(arrayObjectSubset, 20);

    Array arrayObjectSubsetCopy = arrayObjectSubset.copy();
    testArrayObjectIter(arrayObjectSubsetCopy, 20);
  }

  private void testArrayObjectIter(Array a, int expected) {
    int count = 0;
    IndexIterator iter = a.getIndexIterator(); // fast iterator
    while (iter.hasNext()) {
      Object result = iter.next();
      count++;
    }
    assert (count == expected);

    Index index = a.getIndex();
    IndexIterator iter2 = index.getSlowIndexIterator(a); // slow iterator
    count = 0;
    while (iter2.hasNext()) {
      Object result = iter2.next();
      count++;
    }
    assert (count == expected);
  }


}
