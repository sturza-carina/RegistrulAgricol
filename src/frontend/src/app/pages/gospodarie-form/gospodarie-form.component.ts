import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { GospodarieService } from '../../services/gospodarie.service';
import { Gospodarie, Uat } from '../../models/gospodarie.model';
import { SidebarComponent } from '../../components/sidebar/sidebar.component';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-gospodarie-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink, SidebarComponent],
  templateUrl: './gospodarie-form.component.html',
  styleUrls: ['./gospodarie-form.component.css']
})
export class GospodarieFormComponent implements OnInit {
  gospodarieForm: FormGroup;
  isEditMode = false;
  gospodarieId: number | null = null;
  uats: Uat[] = [];
  user: any = null;

  constructor(
    private fb: FormBuilder,
    private gospodarieService: GospodarieService,
    private router: Router,
    private route: ActivatedRoute,
    private http: HttpClient
  ) {
    this.gospodarieForm = this.fb.group({
      codGospodarie: ['', Validators.required],
      tipGospodarie: ['INDIVIDUALA', Validators.required],
      activa: [true],
      uat: [null, Validators.required],
      adresa: this.fb.group({
        county: [''],
        localitate: [''],
        street: [''],
        streetNumber: [''],
        building: [''],
        staircase: [''],
        floor: [''],
        apartmentNumber: [''],
        postalCode: ['']
      })
    });
  }

  ngOnInit() {
    this.loadUats();
    this.route.paramMap.subscribe(params => {
      const id = params.get('id');
      if (id) {
        this.isEditMode = true;
        this.gospodarieId = +id;
        this.loadGospodarie(this.gospodarieId);
      }
    });
  }

  loadUats() {
    this.http.get<Uat[]>('/api/uats').subscribe(data => {
      this.uats = data;
    });
  }

  loadGospodarie(id: number) {
    this.gospodarieService.getGospodarieById(id).subscribe(data => {
      this.gospodarieForm.patchValue(data);
    });
  }

  compareUat(u1: Uat, u2: Uat): boolean {
    return u1 && u2 ? u1.id === u2.id : u1 === u2;
  }

  onSubmit() {
    if (this.gospodarieForm.invalid) return;

    const formData = this.gospodarieForm.value;
    if (this.isEditMode && this.gospodarieId) {
      this.gospodarieService.updateGospodarie(this.gospodarieId, formData).subscribe({
        next: () => this.router.navigate(['/gospodarii']),
        error: (err) => alert('Eroare la salvare')
      });
    } else {
      this.gospodarieService.createGospodarie(formData).subscribe({
        next: () => this.router.navigate(['/gospodarii']),
        error: (err) => alert('Eroare la salvare')
      });
    }
  }

  cancel() {
    this.router.navigate(['/gospodarii']);
  }
}
