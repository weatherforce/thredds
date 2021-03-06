/*
 * Copyright 1998-2015 John Caron and University Corporation for Atmospheric Research/Unidata
 *
 *  Portions of this software were developed by the Unidata Program at the
 *  University Corporation for Atmospheric Research.
 *
 *  Access and use of this software shall impose the following obligations
 *  and understandings on the user. The user is granted the right, without
 *  any fee or cost, to use, copy, modify, alter, enhance and distribute
 *  this software, and any derivative works thereof, and its supporting
 *  documentation for any purpose whatsoever, provided that this entire
 *  notice appears in all copies of the software, derivative works and
 *  supporting documentation.  Further, UCAR requests that the user credit
 *  UCAR/Unidata in any publications that result from the use of this
 *  software or in any product that includes this software. The names UCAR
 *  and/or Unidata, however, may not be used in any advertising or publicity
 *  to endorse or promote any products or commercial entity unless specific
 *  written permission is obtained from UCAR/Unidata. The user also
 *  understands that UCAR/Unidata is not obligated to provide the user with
 *  any support, consulting, training or assistance of any kind with regard
 *  to the use, operation and performance of this software nor to provide
 *  the user with any updates, revisions, new versions or "bug fixes."
 *
 *  THIS SOFTWARE IS PROVIDED BY UCAR/UNIDATA "AS IS" AND ANY EXPRESS OR
 *  IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL UCAR/UNIDATA BE LIABLE FOR ANY SPECIAL,
 *  INDIRECT OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING
 *  FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT,
 *  NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION
 *  WITH THE ACCESS, USE OR PERFORMANCE OF THIS SOFTWARE.
 */
package ucar.nc2.util;

import ucar.unidata.util.StringUtil2;

import java.io.File;
import java.net.URI;

/**
 * Networking utilities.
 *
 * @author caron
 */
public class URLnaming {

  /// try to figure out if we need to add file: to the location when writing
  static public String canonicalizeWrite(String location) {
    try {
      URI refURI = URI.create(location);
      if (refURI.isAbsolute())
        return location;
    } catch (Exception e) {
      //return "file:" + location;
    }
    return "file:" + location;
  }

  /**
   * This augments URI.resolve(), by also dealing with file: URIs.
   * If baseURi is not a file: scheme, then URI.resolve is called.
   * Otherwise the last "/" is found in the base, and the ref is appended to it.
   * <p> For file: baseURLS: only reletive URLS not starting with / are supported. This is
   * apparently different from the behavior of URI.resolve(), so may be trouble,
   * but it allows NcML absolute location to be specified without the file: prefix.
   * <p/>
   * Example : <pre>
   * base:     file://my/guide/collections/designfaq.ncml
   * ref:      sub/my.nc
   * resolved: file://my/guide/collections/sub/my.nc
   * </pre>
   *
   * @param baseUri     base URI as a Strng
   * @param relativeUri reletive URI, as a String
   * @return the resolved URI as a String
   */
  public static String resolve(String baseUri, String relativeUri) {
    if ((baseUri == null) || (relativeUri == null))
      return relativeUri;

    if (relativeUri.startsWith("file:"))
      return relativeUri;

    // deal with a base file URL
    if (baseUri.startsWith("file:")) {

      // the case where the reletiveURL is absolute.
      // unfortunately, we may get an Exception
      try {
        URI uriRelative = URI.create(relativeUri);
        if (uriRelative.isAbsolute())
          return relativeUri;
      } catch (Exception e) {
        // empty
      }

      if ((relativeUri.length() > 0) && (relativeUri.charAt(0) == '#'))
        return baseUri + relativeUri;

      if ((relativeUri.length() > 0) && (relativeUri.charAt(0) == '/'))
        return relativeUri;

      baseUri = StringUtil2.substitute(baseUri, "\\", "/"); // assumes forward slash
      int pos = baseUri.lastIndexOf('/');
      if (pos > 0) {
        String baseDir = baseUri.substring(0, pos + 1);
        if (relativeUri.equals(".")) {
          return baseDir;
        } else {
          return baseDir + relativeUri;
        }
      }
    }

    // non-file URLs

    //relativeUri = canonicalizeRead(relativeUri);
    try {
      URI relativeURI = URI.create(relativeUri);
      if (relativeURI.isAbsolute())
        return relativeUri;

      //otherwise let the URI class resolve it
      URI baseURI = URI.create(baseUri);
      URI resolvedURI = baseURI.resolve(relativeURI);
      return resolvedURI.toASCIIString();

    } catch (IllegalArgumentException e) {
      return  relativeUri;
    }
  }

  public static String resolveFile(String baseDir, String filepath) {
    if (baseDir == null) return filepath;
    if (filepath == null) return null;
    File file = new File(filepath);
    if (file.isAbsolute()) return filepath;

    if (baseDir.startsWith("file:"))
      baseDir = baseDir.substring(5);

    File base = new File(baseDir);
    if (!base.isDirectory())
      base = base.getParentFile();
    if (base == null) return filepath;
    return base.getAbsolutePath() + "/" + filepath;
  }

}
