/*******************************************************************************
 * Copyright (c) 2010 EclipseSource
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     EclipseSource - initial API and implementation
 ******************************************************************************/

package com.eclipsesource.widgets.gmaps;
 
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.jface.dialogs.*;

public class GMapDemo implements IApplication {
  
  static final private String INIT_CENTER = "33.0,5.0";
  static final private int INIT_ZOOM = 2;
  static final private int INIT_TYPE = GMap.TYPE_HYBRID;
  private GMap gmap = null;
 
  public Object start( IApplicationContext context ) throws Exception {
    Display display = new Display();
    final Shell shell = new Shell( display );
    shell.setText( "GMap Widget Demo" );
    shell.setLayout( new FillLayout() );
    SashForm sash = new SashForm( shell, SWT.HORIZONTAL );    
    createMap( sash );
    Composite controls = new Composite( sash, SWT.BORDER );
    controls.setLayout( new GridLayout( 1, true ) );
    createCenterControl( display, controls );
    createZoomControl( controls );
    createMapTypeControl( controls );
    createAddressControl( display, controls );
    createMarkerControl( display, controls );
    sash.setWeights( new int[] { 7, 2 } );
    shell.setSize( 900, 500 );
    shell.open();
    while (!shell.isDisposed()) {
        if (!display.readAndDispatch()) {
          display.sleep();
        }
    }
    display.dispose();
    return IApplication.EXIT_OK;
  }

  private void createMap( final Composite parent ) {
    gmap = new GMap( parent, SWT.NONE );
    gmap.setCenter( stringToLatLng( INIT_CENTER ) );
    gmap.setZoom( INIT_ZOOM );
    gmap.setType( INIT_TYPE );
  }

  private void createCenterControl( Display display, Composite parent ) {
    new Label( parent, SWT.None ).setText( "Location:" );
    final Text location = new Text( parent, SWT.BORDER );
    location.setLayoutData( new GridData( SWT.FILL, SWT.TOP, true, false ) );
    location.setText( INIT_CENTER );
    location.setFont( new Font(display, "Arial", 9, SWT.NORMAL ) );
    location.addModifyListener( new ModifyListener() {
      public void modifyText( ModifyEvent event ) {
        gmap.setCenter( stringToLatLng( location.getText() ) );
      }
    } );
    gmap.addMapListener( new MapAdapter() {
      public void centerChanged() {
        location.setText( gmap.getCenter().toString() );
      }
    } );
  }

  private void createZoomControl( Composite controls ) {
    new Label(controls, SWT.None ).setText( "Zoom:" );
    final Spinner zoom = new Spinner( controls, SWT.NORMAL );
    zoom.setMaximum( 20 );
    zoom.setMinimum( 0 );
    zoom.setSelection( INIT_ZOOM );
    zoom.addModifyListener( new ModifyListener() {
      public void modifyText( ModifyEvent event ) {
        gmap.setZoom( Integer.parseInt( zoom.getText() ) );
      }
    } );
    gmap.addMapListener( new MapAdapter() {
      public void zoomChanged() {
        zoom.setSelection( gmap.getZoom() );              
      };
    } );
  }

  private void createMapTypeControl( Composite parent ) {
    new Label( parent, SWT.None ).setText( "Type:" );
    final Combo type = new Combo( parent, SWT.DROP_DOWN | SWT.READ_ONLY );
    type.setItems( new String[]{
      "ROADMAP",
      "SATELLITE",
      "HYBRID",
      "TERRAIN"
    } );
    type.setText( "HYBRID" );
    type.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        int index = type.getSelectionIndex();
        if( index != -1 ) {
          gmap.setType( index );
        }
      }
    } );
  }

  private void createAddressControl( Display display, Composite parent ) {
    new Label( parent, SWT.None ).setText( "Address:" );
    final Text addr = new Text( parent, SWT.BORDER );
    addr.setLayoutData( new GridData( SWT.FILL, SWT.TOP, true, false ) );
    addr.addSelectionListener( new SelectionAdapter() {
      public void widgetDefaultSelected( SelectionEvent e ) {
        gmap.gotoAddress( addr.getText() );
      }
    } );
    addr.setFont( new Font(display, "Arial", 9, SWT.NORMAL ) );
    Button goToAddr = new Button( parent, SWT.PUSH );
    goToAddr.setText( "go to" );
    goToAddr.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        gmap.gotoAddress( addr.getText() );
      }
    } );
    Button resolveAddr = new Button( parent, SWT.PUSH );
    resolveAddr.setText( "resolve" );
    resolveAddr.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        gmap.resolveAddress();
      }
    } );
    gmap.addMapListener( new MapAdapter() {
      public void addressResolved() {
        addr.setText( gmap.getAddress() );
      }
    } );
  }

  private void createMarkerControl( Display display, Composite controls ) {
    final InputDialog markerDialog = new InputDialog( controls.getShell(), 
                                                     "Marker Name", 
                                                     "Enter Name", 
                                                     null, 
                                                     null );
    Button addMarker = new Button( controls, SWT.PUSH );
    addMarker.setText( "add Marker" );
    addMarker.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        markerDialog.open();
        String result = markerDialog.getValue();
        if( result != null && result.length() > 0 ) {
          gmap.addMarker( result );
        }
      }
    } );    
  }
  
  private LatLng stringToLatLng( final String input ) {
    LatLng result = null;
    if( input != null ) {
      String temp[] = input.split( "," );
      if( temp.length == 2 ) {
        try {
          double lat = Double.parseDouble( temp[ 0 ] );
          double lon = Double.parseDouble( temp[ 1 ] );
          result = new LatLng( lat, lon );
        } catch ( NumberFormatException ex ) {
        }
      }
    }
    return result;
  }

  public void stop() {
  }

}
