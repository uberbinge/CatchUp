/*
 * Copyright (C) 2019. Zac Sweers
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.sweers.catchup.gemoji

import android.content.Context
import androidx.room.Room
import com.squareup.anvil.annotations.ContributesTo
import dagger.Module
import dagger.Provides
import dev.zacsweers.catchup.di.AppScope
import dev.zacsweers.catchup.di.SingleIn
import io.sweers.catchup.util.injection.qualifiers.ApplicationContext

@ContributesTo(AppScope::class)
@Module
object GemojiModule {

  @Provides
  @SingleIn(AppScope::class)
  internal fun provideGemojiDatabase(@ApplicationContext context: Context): GemojiDatabase {
    return Room.databaseBuilder(context, GemojiDatabase::class.java, "gemoji.db")
      .fallbackToDestructiveMigration()
      .createFromAsset("databases/gemoji.db")
      .build()
  }

  @Provides
  @SingleIn(AppScope::class)
  internal fun provideGemojiDao(gemojiDatabase: GemojiDatabase): GemojiDao {
    return gemojiDatabase.gemojiDao()
  }
}
