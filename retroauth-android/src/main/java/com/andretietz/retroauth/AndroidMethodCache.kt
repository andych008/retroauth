/*
 * Copyright (c) 2016 Andre Tietz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.andretietz.retroauth

import android.util.SparseArray

/**
 * Since [SparseArray] is on Android slightly faster, you can use this instead of the default implementation
 * [MethodCache.DefaultMethodCache]
 */
class AndroidMethodCache : MethodCache<String, AndroidCredentialType> {
  private val cache = SparseArray<RequestType<String, AndroidCredentialType>>()

  override fun register(requestIdentifier: Int, type: RequestType<String, AndroidCredentialType>) {
    cache.put(requestIdentifier, type)
  }

  override fun getCredentialType(requestIdentifier: Int) = cache[requestIdentifier]
}
