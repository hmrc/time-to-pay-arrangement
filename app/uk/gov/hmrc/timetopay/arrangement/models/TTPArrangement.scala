package uk.gov.hmrc.timetopay.arrangement.models

import java.time.LocalDate

case class TTPArrangement( identifier: Option[String],
                           createdOn: Option[LocalDate],
                           paymentPlanReference: String,
                           directDebitReference: String,
                           taxpayer: Taxpayer,
                           schedule: Schedule) {

}