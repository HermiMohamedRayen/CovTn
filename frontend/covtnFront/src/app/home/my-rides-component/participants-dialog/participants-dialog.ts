import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';

@Component({
  selector: 'app-participants-dialog',
  templateUrl: './participants-dialog.html',
  styleUrls: ['./participants-dialog.css'],
  standalone: false
})
export class ParticipantsDialog {
  constructor(
    public dialogRef: MatDialogRef<ParticipantsDialog>,
    @Inject(MAT_DIALOG_DATA) public data: { participants: any[] }
  ) {}

  close(): void {
    this.dialogRef.close();
  }
}
