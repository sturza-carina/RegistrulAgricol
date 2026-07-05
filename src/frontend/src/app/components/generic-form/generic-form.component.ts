import { Component, Input, Output, EventEmitter, OnInit, OnChanges, SimpleChanges, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators, FormControl } from '@angular/forms';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { FormConfig, FormField } from './generic-form.models';

import { AppTranslatePipe } from '../../services/translate.pipe';

@Component({
  selector: 'app-generic-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, AppTranslatePipe],
  templateUrl: './generic-form.component.html',
  styleUrls: ['./generic-form.component.css']
})
export class GenericFormComponent implements OnInit, OnChanges, OnDestroy {
  @Input() config!: FormConfig;
  @Input() initialData: any = {};
  @Input() isSubmitting = false;

  @Output() formSubmit = new EventEmitter<any>();
  @Output() formCancel = new EventEmitter<void>();
  // 1. Adăugăm decoratorul de Output pentru a anunța părintele când se modifică un câmp
  @Output() fieldChange = new EventEmitter<{ fieldName: string, value: any }>();

  formGroup!: FormGroup;
  private destroy$ = new Subject<void>();

  constructor(private fb: FormBuilder) { }

  ngOnInit(): void {
    this.initForm();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['config'] && !changes['config'].firstChange) {
      const currentValues = this.formGroup ? this.formGroup.getRawValue() : {};
      this.initForm();
      if (this.formGroup && currentValues) {
        setTimeout(() => {
          this.formGroup.patchValue(currentValues, { emitEvent: false });
        });
      }
    }
    // Când config-ul se schimbă din exterior (de exemplu când modificăm opțiunile unui select din părinte)
    // vrem să ne asigurăm că valorile deja selectate în formular sunt păstrate corect și dropdown-urile dezactivate se reactivează dacă e cazul
    if (changes['config'] && this.formGroup) {
      this.config.sections.forEach(section => {
        section.fields.forEach(field => {
          const control = this.formGroup.get(field.name);
          if (control) {
            if (field.disabled && control.enabled) {
              control.disable({ emitEvent: false });
            } else if (!field.disabled && control.disabled && this.shouldShowField(field)) {
              control.enable({ emitEvent: false });
            }
          }
        });
      });
    }
    if (changes['initialData'] && !changes['initialData'].firstChange && this.formGroup) {
      setTimeout(() => {
        this.formGroup.patchValue(this.initialData || {}, { emitEvent: false });
      });
    }
  }

  initForm(): void {
    if (!this.config) return;

    // Destroy previous subscriptions if we are re-initializing the form
    this.destroy$.next();

    const group: any = {};

    this.config.sections.forEach(section => {
      section.fields.forEach(field => {
        const validators = [];
        if (field.required) {
          validators.push(Validators.required);
        }
        if (field.type === 'email') {
          validators.push(Validators.email);
        }

        const control = new FormControl(
          { value: this.initialData[field.name] !== undefined ? this.initialData[field.name] : (field.type === 'checkbox' ? false : (field.type === 'multi-select' ? [] : '')), disabled: field.disabled || false },
          validators
        );
        group[field.name] = control;
      });
    });

    this.formGroup = this.fb.group(group);

    this.updateFieldVisibility();

    // 2. Înregistrăm un ascultător inteligent pe fiecare câmp din formular în mod individual
    this.config.sections.forEach(section => {
      section.fields.forEach(field => {
        const control = this.formGroup.get(field.name);
        if (control) {
          control.valueChanges.pipe(takeUntil(this.destroy$)).subscribe(value => {
            // Trimitem evenimentul către componenta părinte (GospodarieFormComponent)
            this.fieldChange.emit({ fieldName: field.name, value: value });
            this.updateFieldVisibility();
          });
        }
      });
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  updateFieldVisibility(): void {
    const rawValue = this.formGroup.getRawValue();
    this.config.sections.forEach(section => {
      section.fields.forEach(field => {
        if (field.showIf) {
          const shouldShow = field.showIf(rawValue);
          const control = this.formGroup.get(field.name);
          if (control) {
            if (shouldShow && control.disabled && !field.disabled) {
              control.enable({ emitEvent: false });
            } else if (!shouldShow && control.enabled) {
              control.disable({ emitEvent: false });
            }
          }
        }
      });
    });
  }

  shouldShowField(field: FormField): boolean {
    if (!field.showIf) return true;
    return field.showIf(this.formGroup.getRawValue());
  }

  onSubmit(): void {
    if (this.formGroup.invalid) {
      alert('Vă rugăm să completați corect toate câmpurile obligatorii (marcate cu *). Verificați dacă există erori de validare.');
      this.formGroup.markAllAsTouched();
      return;
    }
    this.formSubmit.emit(this.formGroup.getRawValue());
  }

  onCancel(): void {
    this.formCancel.emit();
  }
}