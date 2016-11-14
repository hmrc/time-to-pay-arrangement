package uk.gov.hmrc.timetopay.arrangement.models

import java.time.LocalDate

case class Taxpayer(customerName: String,
                    addresses: List[Address],
                    selfAssessment: SelfAssessment)

case class SelfAssessment(utr: String,
                          communicationPreferences: Option[CommunicationPreferences],
                          debits: List[Debit])

case class Address(addressLine1: String = "",
                   addressLine2: String = "",
                   addressLine3: String = "",
                   addressLine4: String = "",
                   addressLine5: String = "",
                   postCode: String = "")

case class CommunicationPreferences(welshLanguageIndicator: Boolean,
                                    audioIndicator: Boolean,
                                    largePrintIndicator: Boolean,
                                    brailleIndicator: Boolean)

case class Debit(originCode: String, dueDate: LocalDate)

case class DesDebit(debitType: String, dueDate: LocalDate)