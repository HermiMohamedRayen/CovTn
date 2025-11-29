import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, shareReplay } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class MapService {
  private http: HttpClient;

  constructor(http: HttpClient) {
    this.http = http;
  }

  private getOldGeoLoc(loc : {latitude: number, longitude: number}): any{
    const geoCode = localStorage.getItem('geoCode');
    if(geoCode){
      try {
        const geoObj : Array<any> = JSON.parse(geoCode);
        const found = geoObj.find(geo => geo.latitude === String(loc.latitude) && geo.longitude === String(loc.longitude));
        return found || null;
      } catch (e) {
        console.error('Error parsing geoCode', e);
        return null;
      }
    }
    return null;
  }

   private setOldGeoLoc(lat: number, lon: number, data: any): void{
    const geoCode = localStorage.getItem('geoCode');
    let geoObj : Array<any> = [];
    if(geoCode){
      try {
        geoObj = JSON.parse(geoCode);
      } catch (e) {
        geoObj = [];
      }
    }
    
    const exists = geoObj.some(geo => geo.latitude === String(lat) && geo.longitude === String(lon));
    if (exists) return;

    const less = {
      latitude: String(lat), 
      longitude: String(lon),
      display_name: data.display_name, 
      address: data.address,
      name: data.name
    };
    geoObj.push(less);
    
    // Limit cache size
    if (geoObj.length > 200) {
      geoObj.shift();
    }

    localStorage.setItem('geoCode', JSON.stringify(geoObj));
  }

  public reverseGeocode(latitude: number, longitude: number) : Observable<Object> {
    const url = `https://nominatim.openstreetmap.org/reverse?format=json&lat=${latitude}&lon=${longitude}`;
    const obs = { latitude, longitude };
    const code = this.getOldGeoLoc(obs);
    if(code){
      return new Observable((subscriber) => {
        subscriber.next(code);
        subscriber.complete();
      });
    }
    return new Observable((subscriber) => {
      this.http.get(url).subscribe(
        { next: (data) => {
            this.setOldGeoLoc(latitude, longitude, data);
            subscriber.next(data);
            subscriber.complete();
          }, error: (err) => {
            subscriber.error(err);
          },
        }
      );
    });
  }
  
  
  public geoLocDistance(lat1: number, lon1: number, lat2: number, lon2: number):number {
    return Math.sqrt(Math.pow((lat2 - lat1) * 111.32, 2) + Math.pow((lon2 - lon1) * 40075 * Math.cos(((lat1 + lat2) / 2) * Math.PI / 180) / 360, 2));
  }

  public geocode(address: string) : Observable<Object> {
    const url = `https://nominatim.openstreetmap.org/search?format=jsonv2&q=${encodeURIComponent(address)}`;
    return this.http.get(url);
  }

  public calculateZoomLevel(distanceKm: number): number {
    const z = Math.floor( Math.log2(distanceKm) + 0.000000000000001/5);
    return 14 - z;
  }

  public getUserLocation(): Promise<any> {
    return new Promise((resolve, reject) => {
          navigator.geolocation.getCurrentPosition(
        // Success callback
        (position) => {
            resolve({
              latitude: position.coords.latitude,
              longitude: position.coords.longitude,
              accuracy: position.coords.accuracy
            });

            // Further processing with the location data
        },
        // Error callback
        (error) => {
            switch (error.code) {
                case error.PERMISSION_DENIED:
                    console.error("User denied the request for Geolocation.");
                    break;
                case error.POSITION_UNAVAILABLE:
                    console.error("Location information is unavailable.");
                    break;
                case error.TIMEOUT:
                    console.error("The request to get user location timed out.");
                    break;
                
            }
            reject(error);
        },
        // Optional options object
        {
            enableHighAccuracy: true, // Request high accuracy (e.g., GPS)
            timeout: 5000,          // Maximum time (in ms) to wait for a position
            maximumAge: 0           // Don't use cached position, get a fresh one
        }
    );
    });
  }
  
}
