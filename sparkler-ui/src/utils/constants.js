import dotenv from 'dotenv';
import 'location-origin';

dotenv.config();

export const ROUTER_BASENAME = process.env.REACT_APP_ROUTER_BASENAME;

let a;
if(process.env.REACT_APP_API_URL) {
    a = process.env.REACT_APP_API_URL
}
else {
    a = ""
}

export const API_URL = a
