export default class ReportDto {
  /**
   * @param {LocalDate} weekStart     Startdatum (Montag)
   * @param {LocalDate} weekEnd       Enddatum (Sonntag)
   * @param {number} reportNumber     Für die Reihenfolge der Berichte
   * @param {string} weekText         Betriebliche Tätigkeiten
   * @param {string} instructionText  Unterweisungen und sowas
   * @param {string} schoolText       Themen des Berufsschulunterrichts
   * @param {string} extraText        Sonstiges (Urlaub/Krankheit)
   * @param {string} department       Abteilung
   * @param {boolean} submitted       Azubi hat den Bericht abgegeben
   * @param {boolean} approved        Supervisor hat die Abgabe genehmigt
   * @param {boolean} rejected        Supervisor hat die Abgabe abgelehnt
   * @param {string} comment          Eventuelle Kommentare von Azubi/Supervisor
   * @param {string} approvedBy       Supervisor that approved this report
   */
  constructor(weekStart, weekEnd, reportNumber,weekText, instructionText, schoolText, extraText, department, submitted, approved, rejected, comment, approvedBy) {
    this.weekStart = weekStart;
    this.weekEnd = weekEnd;
    this.reportNumber = reportNumber;
    this.weekText = weekText;
    this.instructionText = instructionText;
    this.schoolText = schoolText;
    this.extraText = extraText;
    this.department = department;
    this.submitted = submitted;
    this.approved = approved;
    this.rejected = rejected;
    this.comment = comment;
    this.approvedBy = approvedBy;
  }

  static fromObject(obj) {
    return new ReportDto(
      obj.weekStart,
      obj.weekEnd,
      obj.reportNumber,
      obj.weekText,
      obj.instructionText,
      obj.schoolText,
      obj.extraText,
      obj.department,
      obj.submitted,
      obj.approved,
      obj.rejected,
      obj.comment,
      obj.approvedBy
    );
  }
}
