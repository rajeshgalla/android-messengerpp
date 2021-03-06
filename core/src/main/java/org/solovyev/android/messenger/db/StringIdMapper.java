/*
 * Copyright 2013 serso aka se.solovyev
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

package org.solovyev.android.messenger.db;

import android.database.Cursor;

import javax.annotation.Nonnull;

import org.solovyev.common.Converter;

/**
 * User: serso
 * Date: 6/9/12
 * Time: 9:17 PM
 */
public class StringIdMapper implements Converter<Cursor, String> {

	@Nonnull
	private static final StringIdMapper instance = new StringIdMapper();

	private StringIdMapper() {
	}

	@Nonnull
	public static StringIdMapper getInstance() {
		return instance;
	}

	@Nonnull
	@Override
	public String convert(@Nonnull Cursor cursor) {
		return cursor.getString(0);
	}
}
