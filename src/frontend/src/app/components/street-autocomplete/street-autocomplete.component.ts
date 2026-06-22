import { Component, Input, OnDestroy, forwardRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';
import { Subject } from 'rxjs';
import { debounceTime, distinctUntilChanged, switchMap } from 'rxjs/operators';
import { StreetAutocompleteService } from '../../services/street-autocomplete.service';

@Component({
  selector: 'app-street-autocomplete',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './street-autocomplete.component.html',
  styleUrls: ['./street-autocomplete.component.css'],
  providers: [{
    provide: NG_VALUE_ACCESSOR,
    useExisting: forwardRef(() => StreetAutocompleteComponent),
    multi: true
  }]
})
export class StreetAutocompleteComponent implements ControlValueAccessor, OnDestroy {
  @Input() city = '';
  @Input() county = '';
  @Input() placeholder = '';

  value = '';
  suggestions: string[] = [];
  showSuggestions = false;
  disabled = false;

  private query$ = new Subject<string>();
  private onChange: (value: string) => void = () => {};
  private onTouched: () => void = () => {};

  constructor(private streetService: StreetAutocompleteService) {
    this.query$.pipe(
      debounceTime(400),
      distinctUntilChanged(),
      switchMap(q => this.streetService.search(q, this.city, this.county))
    ).subscribe(results => {
      this.suggestions = results;
      this.showSuggestions = results.length > 0;
    });
  }

  writeValue(value: string): void {
    this.value = value || '';
  }

  registerOnChange(fn: (value: string) => void): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: () => void): void {
    this.onTouched = fn;
  }

  setDisabledState(isDisabled: boolean): void {
    this.disabled = isDisabled;
  }

  onInput(value: string) {
    this.value = value;
    this.onChange(value);
    this.query$.next(value);
  }

  selectSuggestion(s: string) {
    this.value = s;
    this.onChange(s);
    this.suggestions = [];
    this.showSuggestions = false;
  }

  onFocus() {
    this.showSuggestions = this.suggestions.length > 0;
  }

  onBlur() {
    // Delay so a (mousedown) selection on a suggestion registers before the list is hidden.
    setTimeout(() => { this.showSuggestions = false; }, 200);
    this.onTouched();
  }

  ngOnDestroy() {
    this.query$.complete();
  }
}
