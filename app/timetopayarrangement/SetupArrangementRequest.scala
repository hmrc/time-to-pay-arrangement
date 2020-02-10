package timetopayarrangement

import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.timetopay.arrangement.{Schedule, TaxpayerDetails}

case class SetupArrangementRequest(
    paymentPlanReference: String,
    directDebitReference: String,
    taxpayer:             TaxpayerDetails,
    schedule:             Schedule
)

object SetupArrangementRequest {

  implicit val format: OFormat[SetupArrangementRequest] = Json.format[SetupArrangementRequest]
}