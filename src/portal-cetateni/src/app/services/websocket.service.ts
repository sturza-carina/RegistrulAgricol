import { Injectable } from '@angular/core';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { BehaviorSubject } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class WebsocketService {
  private stompClient!: Client;
  private currentSubscription: any = null;
  public cerereUpdates = new BehaviorSubject<any>(null);

  constructor() {}

  connect(userId: number) {
    if (this.stompClient && this.stompClient.active) {
      this.subscribeToUserTopic(userId);
      return;
    }

    this.stompClient = new Client({
      webSocketFactory: () => new SockJS('/api/ws'),
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000
    });

    this.stompClient.onConnect = (frame) => {
      console.log('Connected to WebSocket');
      this.subscribeToUserTopic(userId);
    };

    this.stompClient.onStompError = (frame) => {
      console.error('Broker reported error: ' + frame.headers['message']);
      console.error('Additional details: ' + frame.body);
    };

    this.stompClient.activate();
  }

  private subscribeToUserTopic(userId: number) {
    if (this.currentSubscription) {
      this.currentSubscription.unsubscribe();
    }
    
    const topic = `/topic/user/${userId}/cereri`;
    this.currentSubscription = this.stompClient.subscribe(topic, (message) => {
      if (message.body) {
        const payload = JSON.parse(message.body);
        this.cerereUpdates.next(payload);
      }
    });
  }

  disconnect() {
    if (this.currentSubscription) {
      this.currentSubscription.unsubscribe();
      this.currentSubscription = null;
    }
    if (this.stompClient && this.stompClient.active) {
      this.stompClient.deactivate();
    }
  }
}
