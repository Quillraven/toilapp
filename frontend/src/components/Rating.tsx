import React, {useEffect} from 'react';
import Box from '@material-ui/core/Box';

export type RatingSizes = "XS" | "S" | "M" | "L"

export function RatingView(props: { toiletId: string, rating: number, size?: RatingSizes }) {
    const sizeNum = getSizeNum(props.size ? props.size : 'M');

    return (
        <Box display="flex" flex={1} flexDirection="row" justifyContent="left">
            {[0, 1, 2, 3, 4].map((idx) => (
                <div style={{width: sizeNum, height: sizeNum}} key={`RatingViewDiv-${props.toiletId}-${idx}`}>
                    <RatingElement rating={props.rating} index={idx}/>
                </div>
            ))}
        </Box>
    );

}

export function RatingSelect(props: { toiletId: string, rating?: number, size?: RatingSizes, onRatingChange: (rating: number) => void }) {
    const sizeNum = getSizeNum(props.size ? props.size : 'M');
    const rating = props.rating ? props.rating : 0

    const lockedRatingRef = React.useRef(rating)
    const [displayRating, setDisplayRating] = React.useState(rating)

    useEffect(() => {
        console.log(`Updating RatingSelect value to '${rating}'`)
        lockedRatingRef.current = rating
        setDisplayRating(lockedRatingRef.current)
    }, [rating])

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
        <Box display="flex" flex="1" flexDirection="row" justifyContent="left">
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

function getSizeNum(size: RatingSizes): number {
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
    } else if (value <= 0.75) {
        iconName = 'poo-half';
    } else {
        iconName = 'poo';
    }
    let iconUrl = `rating/${iconName}.png`
    return <img style={{width: '100%', height: '100%'}} src={iconUrl} alt={`Rating${props.rating}`}/>
}
