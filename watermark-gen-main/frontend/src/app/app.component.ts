import { Component } from '@angular/core';
import {ApiService} from "./service/api.service";

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  title = 'splash';

  constructor(private apiService: ApiService) {}
  selectedFile?: File ;
  subed: boolean = false;
  isLoading: boolean = false;

  subscribe(email: string){
    const requestBody = {
      email: email
    };

    this.apiService.sub(requestBody).subscribe(
      (response) => {
        console.log(response)
        this.subed = true
      },
      (error) => {
        console.log('Error sending Base64 string to backend:', error);
      }
    );
  }

  onFileSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length) {
      this.selectedFile = input.files[0];
      console.log(this.selectedFile)
    }
  }

  uploadFileToBackend(marker: string, email: string) {
    this.isLoading = true
    const reader = new FileReader();
    reader.readAsDataURL(this.selectedFile!);
    reader.onload = () => {
      const base64String = reader.result!.toString().split(',')[1];
      this.sendBase64StringToBackend(marker, base64String, email);
    };
    reader.onerror = (error) => {
      console.log('Error: ', error);
    };
  }

  sendBase64StringToBackend(marker: string, base64String: string, email: string) {
    const requestBody = {
      email: email,
      pdf: base64String,
      watermark: marker
    };

    this.apiService.send(requestBody).subscribe(
      (response) => {
        const jsonObject = JSON.parse(response);
        if(jsonObject.s3PreSignedUrl!==undefined) {
          this.downloadFile(jsonObject.s3PreSignedUrl, "marked.pdf");
        }else{
          this.subed = true;
          this.isLoading=false;
        }

      },
      (error) => {
        console.log('Error sending Base64 string to backend:', error);
      }
    );
  }

  downloadFile(url: string, fileName: string) {
    this.isLoading = false
    const link = document.createElement('a');
    link.href = url;
    link.download = fileName;
    link.target = '_blank'; // Open in a new tab (optional)
    console.log(url)

    document.body.appendChild(link);
    link.click();


    document.body.removeChild(link);
  }

}
