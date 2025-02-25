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
package io.sweers.catchup.app

import android.app.Application
import com.bugsnag.android.Bugsnag
import com.squareup.anvil.annotations.ContributesTo
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet
import dev.zacsweers.catchup.di.AppScope
import dev.zacsweers.catchup.di.SingleIn
import io.sweers.catchup.BuildConfig
import io.sweers.catchup.app.ApplicationModule.Initializers
import io.sweers.catchup.base.ui.CatchUpObjectWatcher
import javax.inject.Qualifier
import kotlin.annotation.AnnotationRetention.BINARY
import timber.log.Timber

@ContributesTo(AppScope::class)
@Module
object ReleaseApplicationModule {

  @Provides
  @SingleIn(AppScope::class)
  fun provideObjectWatcher(): CatchUpObjectWatcher = CatchUpObjectWatcher.None

  @Qualifier @Retention(BINARY) private annotation class BugsnagKey

  @BugsnagKey
  @Provides
  @SingleIn(AppScope::class)
  fun provideBugsnagKey(): String = BuildConfig.BUGSNAG_KEY

  @Initializers
  @IntoSet
  @Provides
  fun bugsnagInit(application: Application, @BugsnagKey key: String): () -> Unit = {
    Bugsnag.start(application, key)
  }

  @IntoSet
  @Provides
  fun provideBugsnagTree(application: Application, @BugsnagKey key: String): Timber.Tree =
    BugsnagTree().also {
      Bugsnag.start(application, key) // TODO nix this by allowing ordering of inits
      Bugsnag.getClient().addOnError { error ->
        it.update(error)
        true
      }
    }
}
