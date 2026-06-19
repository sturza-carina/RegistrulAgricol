import { Component, Input, Output, EventEmitter, OnInit, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators, FormControl } from '@angular/forms';
import { FormConfig, FormField } from './generic-form.models';

@Component({
  selector: 'app-generic-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './generic-form.component.html',
  styleUrls: ['./generic-form.component.css']
})
export class GenericFormComponent implements OnInit, OnChanges {
  @Input() config!: FormConfig;
  @Input() initialData: any = {};
  @Input() isSubmitting = false;
  
  @Output() formSubmit = new EventEmitter<any>();
  @Output() formCancel = new EventEmitter<void>();

  formGroup!: FormGroup;

  constructor(private fb: FormBuilder) {}

  ngOnInit(): void {
    this.initForm();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['config'] && !changes['config'].firstChange) {
      this.initForm();
    }
    if (changes['initialData'] && !changes['initialData'].firstChange && this.formGroup) {
      this.formGroup.patchValue(this.initialData || {});
    }
  }

  initForm(): void {
    if (!this.config) return;

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
          { value: this.initialData[field.name] !== undefined ? this.initialData[field.name] : (field.type === 'checkbox' ? false : ''), disabled: field.disabled || false },
          validators
        );
        group[field.name] = control;
      });
    });

    this.formGroup = this.fb.group(group);

    // Initial check for visibility to disable hidden fields
    this.updateFieldVisibility();

    // Subscribe to changes to update visibility dynamically
    this.formGroup.valueChanges.subscribe(() => {
      this.updateFieldVisibility();
    });
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
