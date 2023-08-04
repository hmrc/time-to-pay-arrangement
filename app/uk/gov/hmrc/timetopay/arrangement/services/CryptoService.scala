/*
 * Copyright 2023 HM Revenue & Customs
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

package uk.gov.hmrc.timetopay.arrangement.services

import play.api.Configuration
import play.api.libs.json.Json
import uk.gov.hmrc.timetopay.arrangement.model.TTPArrangement
import uk.gov.hmrc.crypto.{Crypted, PlainText, SymmetricCryptoFactory}

import javax.inject.{Inject, Singleton}

@Singleton
class CryptoService @Inject() (configuration: Configuration) {

  private lazy val crypto = SymmetricCryptoFactory.aesGcmCryptoFromConfig("mongodb.encryption", configuration.underlying)

  def encryptTtpa(desSubmissionRequest: TTPArrangement): String = {
    crypto.encrypt(PlainText(Json.stringify(Json.toJson(desSubmissionRequest)))).value
  }

  def decryptTtpa(encrypted: String): Option[TTPArrangement] =
    Json.fromJson[TTPArrangement](Json.parse(crypto.decrypt(Crypted(encrypted)).value)).asOpt

  def encryptAuditTags(tags: Map[String, String]): String = {
    crypto.encrypt(PlainText(Json.stringify(Json.toJson(tags)))).value
  }

  def decryptAuditTags(encrypted: String): Option[Map[String, String]] =
    Json.fromJson[Map[String, String]](Json.parse(crypto.decrypt(Crypted(encrypted)).value)).asOpt

}
