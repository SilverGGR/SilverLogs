export default class WeekInterval {
  /**
   * @param {number} weekNumber  ISO-Wochennummer
   * @param {Date}   start       Startdatum (Montag)
   * @param {Date}   end         Enddatum (Sonntag)
   */
  constructor(weekNumber, start, end) {
    this.weekNumber = weekNumber
    this.start      = start
    this.end        = end
  }
}
