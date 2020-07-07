/*
 * Copyright 2018 Red Hat, Inc.
 *
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Apache License v2.0 which accompanies this distribution.
 *
 *  The Eclipse Public License is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  The Apache License v2.0 is available at
 *  http://www.opensource.org/licenses/apache2.0.php
 *
 *  You may elect to redistribute this code under either of these licenses.
 */
package io.reactiverse.es4x;

import org.graalvm.polyglot.Value;

/**
 * Simple EventEmitter
 */
public interface EventEmitter {

  /**
   * Register a callback for an event.
   *
   * @param eventName the event name
   * @param callback the action to perform
   */
  void on(String eventName, Value callback);

  /**
   * Will trigger the event callback if any on the given event.
   *
   * @param eventName the event.
   * @param args arguments to be passed
   * @return callback arity
   */
  int emit(String eventName, Object... args);
}
