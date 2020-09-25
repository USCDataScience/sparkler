import * as types from "./types";

const modelReducer = (state = [], action) => {
    switch(action.type) {
        case types.NEW_MODEL:
            return { ...state, name: action.payload};
        case types.MODEL_LIST:
            return { ...state, models: action.payload};
        case types.ENABLE_MODEL:
            return { ...state, current_model: action.payload};
        case types.UPDATED_RELEVANCY:
            return { ...state, updated_relevancy: action.payload};
        case types.UPDATE_SEED_URLS:
            return { ...state, seed_urls: action.payload};
        case types.MODEL_STATS:
            return { ...state, model_stats: action.payload};
        case types.CRAWL_STARTED:
            return { ...state, crawl: action.payload};
        case types.CRAWL_STATUS:
            return { ...state, crawl_status: action.payload};
        case types.NEW_CONFIG:
            return { ...state, current_config: action.payload};
        case types.SEED_URLS_LIST:
            return { ...state, seeds: action.payload}

        default:
            return state;
    }
}

export default modelReducer
