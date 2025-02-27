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
package io.sweers.catchup.service.reddit

import com.squareup.anvil.annotations.ContributesMultibinding
import com.squareup.anvil.annotations.ContributesTo
import com.squareup.moshi.Moshi
import dagger.Binds
import dagger.Lazy
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.multibindings.IntoMap
import dev.zacsweers.catchup.appconfig.AppConfig
import dev.zacsweers.catchup.di.AppScope
import io.sweers.catchup.libraries.retrofitconverters.delegatingCallFactory
import io.sweers.catchup.service.api.CatchUpItem
import io.sweers.catchup.service.api.DataRequest
import io.sweers.catchup.service.api.DataResult
import io.sweers.catchup.service.api.Mark.Companion.createCommentMark
import io.sweers.catchup.service.api.Service
import io.sweers.catchup.service.api.ServiceKey
import io.sweers.catchup.service.api.ServiceMeta
import io.sweers.catchup.service.api.ServiceMetaKey
import io.sweers.catchup.service.api.SummarizationInfo
import io.sweers.catchup.service.api.TextService
import io.sweers.catchup.service.reddit.model.RedditLink
import io.sweers.catchup.service.reddit.model.RedditObjectFactory
import io.sweers.catchup.util.data.adapters.EpochInstantJsonAdapter
import javax.inject.Inject
import javax.inject.Qualifier
import kotlinx.datetime.Instant
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory

@Qualifier private annotation class InternalApi

private const val SERVICE_KEY = "reddit"

@ServiceKey(SERVICE_KEY)
@ContributesMultibinding(AppScope::class, boundType = Service::class)
class RedditService
@Inject
constructor(@InternalApi private val serviceMeta: ServiceMeta, private val api: RedditApi) :
  TextService {

  override fun meta() = serviceMeta

  override suspend fun fetch(request: DataRequest): DataResult {
    // We special case the front page
    // TODO use limit from request
    return api.frontPage(request.limit, request.pageKey).let { redditListingRedditResponse ->
      @Suppress("UNCHECKED_CAST")
      val data =
        (redditListingRedditResponse.data.children as List<RedditLink>).mapIndexed { index, link ->
          CatchUpItem(
            id = link.id.hashCode().toLong(),
            title = link.title,
            score = "+" to link.score,
            timestamp = link.createdUtc,
            author = "/u/" + link.author,
            source = link.domain ?: "self",
            tag = link.subreddit,
            itemClickUrl = link.url,
            summarizationInfo = SummarizationInfo.from(link.url, link.selftext),
            mark =
              createCommentMark(
                count = link.commentsCount,
                clickUrl = "https://reddit.com/comments/${link.id}"
              ),
            indexInResponse = index + request.pageOffset,
            serviceId = meta().id,
          )
        }
      DataResult(data, redditListingRedditResponse.data.after)
    }
  }
}

@ContributesTo(AppScope::class)
@Module
abstract class RedditMetaModule {

  @IntoMap
  @ServiceMetaKey(SERVICE_KEY)
  @Binds
  internal abstract fun redditServiceMeta(@InternalApi meta: ServiceMeta): ServiceMeta

  companion object {

    @InternalApi
    @Provides
    @Reusable
    internal fun provideRedditServiceMeta(): ServiceMeta =
      ServiceMeta(
        SERVICE_KEY,
        R.string.reddit,
        R.color.redditAccent,
        R.drawable.logo_reddit,
        firstPageKey = null
      )
  }
}

@ContributesTo(AppScope::class)
@Module(includes = [RedditMetaModule::class])
object RedditModule {

  @InternalApi
  @Provides
  internal fun provideMoshi(upstreamMoshi: Moshi): Moshi {
    return upstreamMoshi
      .newBuilder()
      .add(RedditObjectFactory.INSTANCE)
      .add(Instant::class.java, EpochInstantJsonAdapter())
      .build()
  }

  @InternalApi
  @Provides
  internal fun provideRedditOkHttpClient(client: OkHttpClient): OkHttpClient {
    return client
      .newBuilder()
      .addNetworkInterceptor { chain ->
        var request = chain.request()
        val url = request.url
        request =
          request
            .newBuilder()
            .header("User-Agent", "CatchUp app by /u/pandanomic")
            .url(
              url
                .newBuilder()
                .encodedPath("${url.encodedPath}.json")
                .addQueryParameter("raw_json", "1") // So tokens aren't escaped
                .build()
            )
            .build()
        chain.proceed(request)
      }
      .build()
  }

  @Provides
  internal fun provideRedditApi(
    @InternalApi client: Lazy<OkHttpClient>,
    rxJavaCallAdapterFactory: RxJava3CallAdapterFactory,
    @InternalApi moshi: Moshi,
    appConfig: AppConfig
  ): RedditApi {
    val retrofit =
      Retrofit.Builder()
        .baseUrl(RedditApi.ENDPOINT)
        .delegatingCallFactory(client)
        .addCallAdapterFactory(rxJavaCallAdapterFactory)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .validateEagerly(appConfig.isDebug)
        .build()
    return retrofit.create(RedditApi::class.java)
  }
}
