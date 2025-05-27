import AuthUserDto from 'src/dtos/AuthUserDto.js';
import LocalDate from 'src/dtos/localDate.js';

export default class ApprenticeDto extends AuthUserDto{
  /**
   * @param {LocalDate} startingDate
   * @param {LocalDate} endingDate
   * @param args                      Vererbung von AuthUserDto
   */
  constructor(startingDate, endingDate, ...args) {
    super(...args);
    this.startingDate = startingDate instanceof LocalDate ? startingDate : null;
    this.endingDate = endingDate instanceof LocalDate ? endingDate : null;
  }
}
