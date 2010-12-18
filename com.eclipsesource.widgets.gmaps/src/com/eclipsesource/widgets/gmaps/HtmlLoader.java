/*******************************************************************************
 * Copyright (c) 2010 EclipseSource and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   EclipseSource - initial API and implementation
 ******************************************************************************/
package com.eclipsesource.widgets.gmaps;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.eclipse.swt.browser.Browser;

/**
 * This class can load a local html-file and the local scripts it references 
 * into a browser-widget using setText. This method (and not setUrl) is used 
 * because it is the only way that works in RAP *and* RCP, as they both have 
 * some restrictions.
 * 
 * While in RCP it would be possible to simply load the html form the 
 * file-system, this wont work out-of-the-box if the file is inside a jar.
 * Also, the url would differ in RAP.
 * 
 * As RAP has already a running http-server, the resources can easily be 
 * registered using the "org.eclipse.equinox.http.registry.resources" extension
 * point and loaded from there. Obviously this can not be done in RCP. 
 * 
 * If you are only targeting RAP, the last method is strongly recommended as it 
 * is more efficient, especially when loading the same content several times.
 * 
 * Note that loading the html from another server wont work in RAP due to 
 * the browsers security-restrictions. 
 **/

final class HtmlLoader {
  
  static void load( final Browser browser, final String htmlFile ) {
    browser.setText( getHtmlContent( htmlFile ) );
  }

  private static String getHtmlContent( final String url ) {
    StringBuffer html = getFileContent( url );
    inlineScripts( html );
    return html.toString();
  }

  private static StringBuffer getFileContent( final String file ) {
    StringBuffer buffer = new StringBuffer();
    InputStream stream = 
      HtmlLoader.class.getClassLoader().getResourceAsStream( file );
    if( stream != null ) {
      InputStreamReader inputStreamReader = new InputStreamReader( stream );
      BufferedReader bufferedReader = new BufferedReader( inputStreamReader );
      try {
        String line = null;
        try {
          while( ( line = bufferedReader.readLine() ) != null ) {
            buffer.append( line );
            buffer.append( '\n' );
          }
        } finally {
          bufferedReader.close();
        }
      } catch( IOException e ) {
        buffer.append( "Could not read File: " + e.toString() );
      }
    } else {
      buffer.append( "File not found!" );
    }
    return buffer;    
  }

  private static void inlineScripts( final StringBuffer html ) {
    String srcAttrStr = "src=\"./";
    String quotStr = "\"";
    String tagStr = "<script ";
    String closingTagStr = "</script>";
    String newTagStr = "<script type=\"text/javascript\">";
    int offset = html.length();
    while( ( offset = html.lastIndexOf( tagStr, offset ) ) != -1 ) {
      int closeTag = html.indexOf( closingTagStr, offset );
      int srcAttr = html.indexOf( srcAttrStr, offset );
      if( srcAttr != -1 && srcAttr < closeTag ) {
        int srcAttrStart = srcAttr + srcAttrStr.length();
        int srcAttrEnd = html.indexOf( quotStr, srcAttrStart );
        if( srcAttrEnd != -1 ) {
          String filename = html.substring( srcAttrStart, srcAttrEnd );
          StringBuffer newScriptTag = new StringBuffer();
          newScriptTag.append( newTagStr );
          newScriptTag.append( getFileContent( filename ) );
          newScriptTag.append( closingTagStr );
          html.replace( offset, 
                        closeTag + closingTagStr.length(), 
                        newScriptTag.toString() );
        }
      }
      offset--;
    }
  }


}
