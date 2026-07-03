import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Stadiu } from './stadiu';

describe('Stadiu', () => {
  let component: Stadiu;
  let fixture: ComponentFixture<Stadiu>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Stadiu],
    }).compileComponents();

    fixture = TestBed.createComponent(Stadiu);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
