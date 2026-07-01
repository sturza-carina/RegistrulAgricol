import { Injectable, signal } from '@angular/core';

export type ToastType = 'success' | 'error' | 'warning' | 'info';

export interface Toast {
  id: number;
  message: string;
  type: ToastType;
}

const DEFAULT_DURATION_MS = 4000;

@Injectable({
  providedIn: 'root'
})
export class ToastService {
  private nextId = 0;
  toasts = signal<Toast[]>([]);

  show(message: string, type: ToastType = 'info', durationMs = DEFAULT_DURATION_MS): void {
    const id = this.nextId++;
    this.toasts.update(list => [...list, { id, message, type }]);
    setTimeout(() => this.dismiss(id), durationMs);
  }

  success(message: string, durationMs?: number): void {
    this.show(message, 'success', durationMs);
  }

  error(message: string, durationMs?: number): void {
    this.show(message, 'error', durationMs);
  }

  warning(message: string, durationMs?: number): void {
    this.show(message, 'warning', durationMs);
  }

  info(message: string, durationMs?: number): void {
    this.show(message, 'info', durationMs);
  }

  dismiss(id: number): void {
    this.toasts.update(list => list.filter(t => t.id !== id));
  }
}
