export type FieldType = 'text' | 'number' | 'email' | 'password' | 'date' | 'select' | 'checkbox' | 'textarea';

export interface FormFieldOption {
  label: string;
  value: any;
}

export interface FormField {
  name: string;             // The key in the form data object
  label: string;            // The display label
  type: FieldType;          // Type of the input
  required?: boolean;       // Is it mandatory?
  options?: FormFieldOption[]; // For 'select' type
  placeholder?: string;
  disabled?: boolean;
  min?: number | string;
  max?: number | string;
  step?: number;
  width?: 'full' | 'half' | 'third'; // For grid layout (default: half)
  showIf?: (model: any) => boolean;  // Dynamic visibility based on current form state
  hint?: string;            // Small text under the field
}

export interface FormSection {
  title?: string;
  icon?: string;
  description?: string;
  fields: FormField[];
}

export interface FormConfig {
  sections: FormSection[];
  submitText?: string;
  cancelText?: string;
}
