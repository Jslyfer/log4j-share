/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.log4j.helpers;

import org.apache.log4j.spi.AppenderAttachable;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.Appender;

import java.util.Collections;
import java.util.Enumeration;
import java.util.concurrent.CopyOnWriteArrayList;

/**
   A straightforward CopyOnWriteArrayList-based implementation of the
   {@link AppenderAttachable} interface.
   <p>
   Note that althought the implementation uses CopyOnWriteArrayList (and thus
   requires Java 5, on the whole it is no more thread safe than
   AppenderAttachableImpl (which was not thread-safe despite using Vector).
   The main benefit of using CopyOnWriteArrayList is that
   appendLoopOnAppenders(LoggingEvent) no longer requires <i>any</i>
   synchronization or array copying.
   <p>
   This class is final simply as I see no need to extend it at this time.

   @author Jess Holle
 */
public final class AppenderAttachableImpl5 implements AppenderAttachable {  

  /** Array of appenders. */
  private CopyOnWriteArrayList<Appender>  appenderList;

  /**
     Attach an appender. If the appender is already in the list in
     won't be added again.
  */
  public
  void addAppender(Appender newAppender) {
    // Null values for newAppender parameter are strictly forbidden.
    if(newAppender == null)
      return;
    
    CopyOnWriteArrayList<Appender>  appenderList;    
    synchronized ( this )
    {
      appenderList = this.appenderList;
      if( appenderList == null )
        this.appenderList = appenderList = new CopyOnWriteArrayList<Appender>();
    }
    appenderList.addIfAbsent( newAppender );
  }

  /**
     Call the <code>doAppend</code> method on all attached appenders.
     This method is thread-safe against other methods on this class.
   */
  public
  int appendLoopOnAppenders(LoggingEvent event) {
    // copy into field, so null check and usage are against same value and to reduce field access
    final CopyOnWriteArrayList<Appender>  appenderList = this.appenderList;
    if ( ( appenderList != null ) && !appenderList.isEmpty() ) {
      int size = 0;
      for ( Appender appender : appenderList ) {
        appender.doAppend(event);
        ++size;
      }
      return size;
    }
    return 0;
  }

  /**
     Get all attached appenders as an Enumeration. If there are no
     attached appenders <code>null</code> is returned.
     
     @return Enumeration An enumeration of attached appenders.
   */
  public
  Enumeration getAllAppenders() {
    final CopyOnWriteArrayList<Appender>  appenderList = this.appenderList;
    if(appenderList == null)
      return null;
    else 
      return Collections.enumeration( appenderList );    
  }

  /**
     Look for an attached appender named as <code>name</code>.

     <p>Return the appender with that name if in the list. Return null
     otherwise.  
     
   */
  public
  Appender getAppender(String name) {
     if(name == null)
      return null;
     final CopyOnWriteArrayList<Appender>  appenderList = this.appenderList;
     if(appenderList == null)
      return null;

     for ( Appender appender : appenderList )
       if(name.equals(appender.getName()))
         return appender;
     return null;
  }

  /**
     Returns <code>true</code> if the specified appender is in the
     list of attached appenders, <code>false</code> otherwise.

     @since 1.2 */
  public 
  boolean isAttached(Appender appender) {
    if(appender == null)
      return false;
    final CopyOnWriteArrayList<Appender>  appenderList = this.appenderList;
    if(appenderList == null)
      return false;

    for ( Appender a : appenderList )
      if(a == appender)
        return true;
    return false;    
  }

  /**
   * Remove and close all previously attached appenders.
   * */
  public
  void removeAllAppenders() {
    final CopyOnWriteArrayList<Appender>  appenderList = this.appenderList;
    if(appenderList == null)
      return;
    for ( Appender a : appenderList )
      a.close();
    appenderList.clear();
    synchronized ( this )
    {
      this.appenderList = null;
    }
  }

  /**
     Remove the appender passed as parameter form the list of attached
     appenders.  */
  public
  void removeAppender(Appender appender) {
    if(appender == null) 
      return;
    final CopyOnWriteArrayList<Appender>  appenderList = this.appenderList;
    if(appenderList == null) 
      return;
    appenderList.remove(appender);    
  }

 /**
    Remove the appender with the name passed as parameter form the
    list of appenders.  
  */
  public
  void removeAppender(String name) {
    if(name == null)
      return;
    final CopyOnWriteArrayList<Appender>  appenderList = this.appenderList;
    if(appenderList == null)
      return;
    for ( Appender appender : appenderList )
      if(name.equals(appender.getName())) {
        appenderList.remove(appender);
        break;
      }
  }
}
