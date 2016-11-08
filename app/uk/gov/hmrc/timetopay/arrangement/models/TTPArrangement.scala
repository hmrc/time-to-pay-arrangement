package uk.gov.hmrc.timetopay.arrangement.models

import java.time.{LocalDateTime, LocalDate}

case class TTPArrangement(id: Option[String],
                          createdOn: Option[LocalDateTime],
                          paymentPlanReference: String,
                          directDebitReference: String,
                          taxpayer: Taxpayer,
                          schedule: Schedule,
                          desArrangement : Option[DesSubmissionRequest]) {

}

case class DesTTPArrangement(startDate: LocalDate,
                               endDate: LocalDate,
                               firstPaymentDate: LocalDate,
                               firstPaymentAmount: String,
                               regularPaymentAmount: String,
                               regularPaymentFrequency: String = "Monthly",
                               reviewDate: LocalDate,
                               initials: String = "ZZZ",
                               enforcementAction: String,
                               directDebit: Boolean = true,
                               debitDetails: List[Debit],
                               saNote: String) {

}

case class LetterAndControl(customerName: String,
                            salutation: String = "Dear Sir or Madam",
                            addressLine1: String = "",
                            addressLine2: String = "",
                            addressLine3: String = "",
                            addressLine4: String = "",
                            addressLine5: String = "",
                            postCode: String = "",
                            totalAll: String,
                            clmIndicateInt: String = "Interest is due",
                            clmPymtString: String,
                            officeName1: String = "",
                            officeName2: String = "",
                            officePostcode: String = "",
                            officePhone: String = "",
                            officeFax: String = "",
                            officeOpeningHours: String = "9-5",
                            template: String = "template",
                            exceptionType: Option[String] = None,
                            exceptionReason: Option[String] = None
                           ) {

}

case class DesSubmissionRequest(ttpArrangement: DesTTPArrangement, letterAndControl: LetterAndControl) {}
