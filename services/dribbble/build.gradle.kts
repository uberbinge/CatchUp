/*
 * Copyright (c) 2018 Zac Sweers
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

plugins {
  alias(libs.plugins.sgp.base)
  id("com.android.library")
  kotlin("android")
}

android {
  namespace = "io.sweers.catchup.service.dribbble"
  buildFeatures {
    androidResources = true
  }
}

slack {
  features {
    dagger()
  }
}

dependencies {
  implementation(project(":libraries:retrofitconverters"))
  implementation(project(":libraries:util"))
  implementation(libs.misc.jsoup)
  implementation(libs.retrofit.core)
  implementation(libs.retrofit.rxJava3)
  implementation(libs.okhttp.core)
  implementation(libs.kotlin.datetime)

  api(project(":service-api"))
  api(libs.androidx.annotations)
  api(libs.dagger.runtime)
  api(libs.rx.java)
}
