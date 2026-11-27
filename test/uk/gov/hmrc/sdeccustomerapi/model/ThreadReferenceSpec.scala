/*
 * Copyright 2026 HM Revenue & Customs
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

package uk.gov.hmrc.sdeccustomerapi.model

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

class ThreadReferenceSpec extends AnyWordSpec with Matchers {

  private val validReference = "THREAD1000AA11"

  ".from" must {
    "accept 14 upper case letters and digits" in {
      ThreadReference.from(validReference) mustBe Right(ThreadReference(validReference))
    }

    "reject a reference that is too short" in {
      ThreadReference.from("THREAD1000AA1") mustBe Left("Invalid thread reference")
    }

    "reject a reference that is too long" in {
      ThreadReference.from("THREAD1000AA123456") mustBe Left("Invalid thread reference")
    }

    "reject lower case characters" in {
      ThreadReference.from("thread1000aa11") mustBe Left("Invalid thread reference")
    }

    "reject special characters" in {
      ThreadReference.from("THREAD-100AA-11") mustBe Left("Invalid thread reference")
    }

    "reject an empty reference" in {
      ThreadReference.from("") mustBe Left("Invalid thread reference")
    }
  }

}
