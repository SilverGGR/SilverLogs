export default class ReportDto {
  /**
   * @param {LocalDate} weekStart     Startdatum (Montag)
   * @param {LocalDate} weekEnd       Enddatum (Sonntag)
   * @param {number} weekNumber       Kalenderwoche des Berichts
   * @param {string} weekText         Betriebliche Tätigkeiten
   * @param {string} instructionText  Unterweisungen und sowas
   * @param {string} schoolText       Themen des Berufsschulunterrichts
   * @param {string} extraText        Sonstiges (Urlaub/Krankheit)
   * @param {string} department       Abteilung
   * @param {boolean} submitted       Azubi hat den Bericht abgegeben
   * @param {boolean} approved        Supervisor hat die Abgabe genehmigt
   * @param {boolean} rejected        Supervisor hat die Abgabe abgelehnt
   * @param {string} comment          Eventuelle Kommentare von Azubi/Supervisor
   */
  constructor(weekStart, weekEnd, weekNumber,weekText, instructionText, schoolText, extraText, department, submitted, approved, rejected, comment) {
    this.weekStart = weekStart;
    this.weekEnd = weekEnd;
    this.weekNumber = weekNumber;
    this.weekText = weekText;
    this.instructionText = instructionText;
    this.schoolText = schoolText;
    this.extraText = extraText;
    this.department = department;
    this.submitted = submitted;
    this.approved = approved;
    this.rejected = rejected;
    this.comment = comment;
  }

  static fromObject(obj) {
    return new ReportDto(
      obj.weekStart,
      obj.weekEnd,
      obj.weekNumber,
      obj.weekText,
      obj.instructionText,
      obj.schoolText,
      obj.extraText,
      obj.department,
      obj.submitted,
      obj.approved,
      obj.rejected,
      obj.comment
    );
  }
}
