import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ParcelaMapComponent } from './parcela-map.component';

describe('ParcelaMapComponent', () => {
  let component: ParcelaMapComponent;
  let fixture: ComponentFixture<ParcelaMapComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ParcelaMapComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ParcelaMapComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
