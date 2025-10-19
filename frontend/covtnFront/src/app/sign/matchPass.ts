import { AbstractControl, FormControl, ValidationErrors, ValidatorFn } from "@angular/forms";

/** An actor's name can't match the given regular expression */
export function matchPass(nameRe: FormControl): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const forbidden = nameRe.value !== control.value;
    return forbidden ? {passwordMismatch: {value: control.value}} : null;
  };
}