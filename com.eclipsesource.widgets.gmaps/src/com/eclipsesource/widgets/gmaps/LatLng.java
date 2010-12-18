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


public final class LatLng {
  
  public final double latitude;
  public final double longitude;
  
  public LatLng( double latitude, double longitude ) {
    this.latitude = latitude;
    this.longitude = longitude;
  }

  public String toString() {
    return latitude + "," + longitude;
  }

  public int hashCode() {
    final int prime = 31;
    int result = 1;
    long temp;
    temp = Double.doubleToLongBits( latitude );
    result = prime * result + ( int )( temp ^ ( temp >>> 32 ) );
    temp = Double.doubleToLongBits( longitude );
    result = prime * result + ( int )( temp ^ ( temp >>> 32 ) );
    return result;
  }

  public boolean equals( Object obj ) {
    if( this == obj )
      return true;
    if( obj == null )
      return false;
    if( getClass() != obj.getClass() )
      return false;
    LatLng other = ( LatLng )obj;
    if( Double.doubleToLongBits( latitude ) != Double.doubleToLongBits( other.latitude ) )
      return false;
    if( Double.doubleToLongBits( longitude ) != Double.doubleToLongBits( other.longitude ) )
      return false;
    return true;
  }
  
}
