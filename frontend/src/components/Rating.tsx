import React from 'react';
import Box from '@material-ui/core/Box';

export function RatingView(props: { toiletId: string, rating: number, size?: string }) {
    let size = props.size ? props.size : 'M';
    size = ['XS', 'S', 'M', 'L'].includes(size) ? size : 'M';
    let sizeNum = getSizeNum(size);

    return (
        <Box display="flex" flex={1} flexDirection="row" justifyContent="center">
            {[0, 1, 2, 3, 4].map((idx) => (
                <div style={{width: sizeNum, height: sizeNum}} key={`RatingViewDiv-${props.toiletId}-${idx}`}>
                    <RatingElement rating={props.rating} index={idx}/>
                </div>
            ))}
        </Box>
    );

}

export function RatingSelect(props: { toiletId: string, rating?: number, size?: string, onRatingChange: (rating: number) => void }) {
    let size = props.size ? props.size : 'M';
    size = ['XS', 'S', 'M', 'L'].includes(size) ? size : 'M';
    let sizeNum = getSizeNum(size);

    const lockedRatingRef = React.useRef(props.rating ? props.rating : 0)
    const [displayRating, setDisplayRating] = React.useState(props.rating ? props.rating : 0)
    const ratingLocked = (ratingIdx: number) => {
        lockedRatingRef.current = ratingIdx + 1
        props.onRatingChange(lockedRatingRef.current);
    };
    const mouseEnter = (ratingIdx: number) => {
        setDisplayRating(ratingIdx + 1)
    }
    const mouseExit = () => {
        setDisplayRating(lockedRatingRef.current)
    }

    return (
        <Box display="flex" flex="1" flexDirection="row" justifyContent="center">
            {[0, 1, 2, 3, 4].map((idx) => (
                <div style={{width: sizeNum, height: sizeNum}}
                     onClick={() => ratingLocked(idx)}
                     onMouseEnter={() => mouseEnter(idx)}
                     onMouseLeave={mouseExit}
                     key={`RatingSelectDiv-${props.toiletId}-${idx}`}
                >
                    <RatingElement rating={displayRating} index={idx} key={`RatingElement-${idx}`}/>
                </div>
            ))}
        </Box>
    );
}

function getSizeNum(size: string): number {
    switch (size) {
        case 'XS': {
            return 18
        }
        case 'S': {
            return 32;
        }
        case 'M': {
            return 64;
        }
        case 'L': {
            return 120;
        }
        default: {
            return 32;
        }
    }
}

function RatingElement(props: { rating: number, index: number }) {
    let iconName;
    let value = props.rating - props.index;
    if (value < 0.25) {
        iconName = 'poo-empty';
    } else if (value < 0.75) {
        iconName = 'poo-half';
    } else {
        iconName = 'poo';
    }
    let iconUrl = `rating/${iconName}.png`
    return <img style={{width: '100%', height: '100%'}} src={iconUrl} alt={`Rating${props.rating}`}/>
}
