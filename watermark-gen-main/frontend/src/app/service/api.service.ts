import { Injectable } from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class ApiService {

  host: string = ''

  constructor(private http: HttpClient) { }

  send(requestBody: any): Observable<any> {
    return this.http.post<any>('https://i9xdi95cz1.execute-api.us-east-1.amazonaws.com/production/invite', requestBody);
  }

  sub(requestBody: any): Observable<any> {
    return this.http.post<any>('https://i9xdi95cz1.execute-api.us-east-1.amazonaws.com/production/subscribe', requestBody);
  }
}
