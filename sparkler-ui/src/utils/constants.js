import dotenv from 'dotenv';
import 'location-origin';

dotenv.config();

export const ROUTER_BASENAME = process.env.REACT_APP_ROUTER_BASENAME;

if(process.env.REACT_APP_API_URL) {
    export const API_URL = process.env.REACT_APP_API_URL
}
else {
    export const API_URL = "/"
}
