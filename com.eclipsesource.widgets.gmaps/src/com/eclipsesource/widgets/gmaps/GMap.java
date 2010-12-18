/*******************************************************************************
 * Copyright (c) 2002,2010 Innoopract Informationssysteme GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Innoopract Informationssysteme GmbH - initial API and implementation
 *     EclipseSource - ongoing development
 ******************************************************************************/
package com.eclipsesource.widgets.gmaps;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.BrowserFunction;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.browser.ProgressListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Layout;

public class GMap extends Composite {

  private static final String[] AVAILABLE_TYPES = new String[] {
    "ROADMAP",
    "SATELLITE",
    "HYBRID",
    "TERRAIN"
  };
  public final static int TYPE_ROADMAP = 0;
  public final static int TYPE_SATELLITE = 1;
  public final static int TYPE_HYBRID = 2;
  public final static int TYPE_TERRAIN = 3;
  
  private Browser browser;
  private int type = TYPE_ROADMAP;
  private String address = "";
  private LatLng center = new LatLng( 0, 0 );
  private int zoom = 8;
  private boolean loaded = false;
  private ListenerList listeners = new ListenerList();

  public GMap( final Composite parent, final int style ) {
    super( parent, style );
    super.setLayout( new FillLayout() );
    browser = new Browser( this, SWT.NONE );
    loadMap();
  }
  
  public void setLayout( Layout layout ) {
    checkWidget();
    // prevent setting another layout
  }

  public void setCenter( final LatLng center ) {
    checkWidget();
    if( !this.center.equals( center ) && center != null ) {
      this.center = center;
      if( loaded ) {
        browser.evaluate( "setCenter( [ " + center.toString() + " ] );" );
      }
      fireCenterChanged();
    }
  }
  
  public LatLng getCenter() {
    checkWidget();
    return center;
  }
  
  /**
   * @see GMap#AVAILABLE_TYPES
   */
  public void setType( final int type ) {
    checkWidget();
    if( type < 0 || type > 3 ) {
      throw new IllegalArgumentException( "Illegal map type" );
    }
    this.type = type;
    if( loaded ) {
      browser.evaluate( "setType( " + createJsMapType() + " )" );
    }
  }
  
  public int getType() {
    checkWidget();
    return type;
  }

  /**
   * Zoom can be a value between 0 and 20. 
   * Not all areas have data for all levels.
   */
  public void setZoom( final int zoom ) {
    checkWidget();
    if( zoom < 0 || zoom > 20 ) {
      throw new IllegalArgumentException( "Illegal zoom value" );      
    }
    if( zoom != this.zoom ) {
      this.zoom = zoom;
      if( loaded ) {
        browser.evaluate( "setZoom( " + Integer.toString( zoom ) + " )");
      }
      fireZoomChanged();
    }
  }
  
  public int getZoom() {
    checkWidget();
    return zoom;
  }
  
  /**
   * Sets the location of the map to the best result that matching the address.
   * There will be some delay while the geocoder is queried.
   */
  public void gotoAddress( final String address ) {
    checkWidget();
    if( loaded && address != null ) {
      this.address = address;
      browser.evaluate( "gotoAddress( " + createJsAddress() + " )" );
    }
  }
  
  /**
   * Resolves address of current location (center).
   * Result will be received asynchronously.
   * 
   * @see MapListener#addressResolved()
   */
  public void resolveAddress() {
    checkWidget();
    browser.evaluate( "resolveAddress()" );
  }
  
  /**
   * Returns the last address given or resolved. Will not be updated 
   * automatically as the location changes.
   * 
   * @see GMap#gotoAddress(String)
   * @see GMap#resolveAddress()
   * @see MapListener#addressResolved()
   */
  public String getAddress() {
    checkWidget();
    return address;
  }
  
  /**
   * This adds a draggable marker with a an infowindow to the current center.
   * However, its currently not possible to get the location of the marker
   * should the user move it. 
   */
  public void addMarker( final String name ) {
    checkWidget();
    browser.evaluate( "addMarker( \"" + name + "\" )" );
  }

  public void addMapListener( final MapListener listener ) {
    listeners.add( listener );
  }

  public void removeMapListener( final MapListener listener ) {
    listeners.remove( listener );
  }
  
  //////////////////////////////////
  // map creation and event-handling
  
  private void loadMap() {
    HtmlLoader.load( browser, "GMap.html" );
    browser.addProgressListener( new ProgressListener() {
      public void completed( ProgressEvent event ) {
        // Note: Calling execute/eval before the document is loaded wont work.
        loaded = true;
        StringBuffer script = new StringBuffer();
        script.append( "init( " );
        script.append( "[ " + center.toString() + " ], " );
        script.append( zoom + "," );
        script.append( createJsMapType() );
        script.append( ");" );
        browser.evaluate( script.toString() );
        createBrowserFunctions();
      }     
      public void changed( ProgressEvent event ) {
      }
    } );
  }

  private void createBrowserFunctions() {
    new BrowserFunction( browser, "onBoundsChanged" ) {
      public Object function( Object[] arguments ) {
        syncCenter( ( Double )arguments[ 0 ], ( Double )arguments[ 1 ] );
        syncZoom( ( Double )arguments[ 2 ] );
        return null;
      }
    };
    new BrowserFunction( browser, "onAddressResolved" ) {
      public Object function( Object[] arguments ) {
        resolvedAddress( ( String ) arguments[ 0 ] );
        return null;
      }
    };
  }

  private void syncCenter( final Double latitude, final Double longitude ) {
    LatLng newCenter 
      = new LatLng( latitude.doubleValue(), longitude.doubleValue() );
    if( !center.equals( newCenter ) ) {
      center = newCenter;
      fireCenterChanged();
    }
  }

  private void syncZoom( final Double zoom ) {
    int newZoom = zoom.intValue();
    if( newZoom != this.zoom ) {
      this.zoom = newZoom;
      fireZoomChanged();
    }
  }
  
  private void resolvedAddress( String string ) {
    // TODO : - Failed or obsolete results are handled neither here nor in js. 
    //        - Multiple results are ignored.
    //        - Calling the GeoCoder from client is somewhat unnecessary, could 
    //          be done directly from java using Google Maps Web Services.
    this.address = string;
    fireAddressResolved();
  }

  /////////
  // Helper
  
  private String createJsMapType() {
    String typeStr = AVAILABLE_TYPES[ type ];
    return "google.maps.MapTypeId." + typeStr;
  }
  
  private String createJsAddress() {
    return "\"" + address + "\"";
  }
  
  private void fireCenterChanged() {
    Object[] allListeners = listeners.getListeners();
    for( int i = 0; i < allListeners.length; i++ ) {
      MapListener listener = ( MapListener )allListeners[ i ];
      listener.centerChanged();
    }
  }
  
  private void fireZoomChanged() {
    Object[] allListeners = listeners.getListeners();
    for( int i = 0; i < allListeners.length; i++ ) {
      MapListener listener = ( MapListener )allListeners[ i ];
      listener.zoomChanged();
    }
  }

  
  private void fireAddressResolved() {
    Object[] allListeners = listeners.getListeners();
    for( int i = 0; i < allListeners.length; i++ ) {
      MapListener listener = ( MapListener )allListeners[ i ];
      listener.addressResolved();
    }
  }
  
  
}