import * as types from "./types";

const searchReducer = (state = [], action) => {
    switch(action.type) {
        case types.SEARCH_RESULTS:
            return { ...state, websites: action.payload};
        case types.SEARCH_FIRED:
            return { ...state, running: action.payload};
        default:
            return state;
    }
}

export default searchReducer