import React, {Component} from 'react';
import Card from "./Card/Card"
import GridItem from "./Grid/GridItem"
import CardHeader from "./Card/CardHeader";
import CardBody from "./Card/CardBody";

interface User {
    id: string
    name: string
    email: string
}

interface Comment {
    user: User
    date: Date
    text: string
}

interface GeoPoint {
    x: number
    y: number
}

interface Toilet {
    title: string
    location: GeoPoint
    preview: string
    image: any
    rating: number
    disable: boolean
    toiletCrewApproved: boolean
    description: string
    comments: Array<Comment>
    images: Array<string>
}

interface ToiletListProps {
}

interface ToiletListState {
    Toilets: Array<Toilet>;
    isLoading: boolean;
}

class ToiletList extends Component<ToiletListProps, ToiletListState> {

    constructor(props: ToiletListProps) {
        super(props);

        this.state = {
            Toilets: [],
            isLoading: false
        };
    }

    async componentDidMount() {
        this.setState({isLoading: true});

        const responseToilets = await fetch('http://localhost:3000/api/toilets');
        const toiletData: Toilet[] = await responseToilets.json();
        for (const toilet of toiletData.filter(it => it.preview)) {
            const responseImage = await fetch(`http://localhost:3000/api/previews/${toilet.preview}`)
            const imageBlob = await responseImage.blob()
            toilet.image = URL.createObjectURL(imageBlob)
        }

        this.setState({
            Toilets: toiletData,
            isLoading: false,
        });
    }

    render() {
        const {Toilets, isLoading} = this.state;

        if (isLoading) {
            return <p>Fetching toilets...</p>;
        }

        return (
            <GridItem>
                <Card>
                    <CardHeader color={"danger"}>
                        <h4 className={"cardTitle"}>Toilets</h4>
                    </CardHeader>
                    <CardBody>
                        <table>
                            <thead>
                            <tr>
                                <th>Image</th>
                                <th>Title</th>
                                <th>Location</th>
                                <th>Rating</th>
                            </tr>
                            </thead>
                            <tbody>
                            {Toilets.map((toilet: Toilet) => (
                                <tr>
                                    <td>
                                        <img src={toilet.image} alt={toilet.title} width={150} height={150}/>
                                    </td>
                                    <td>{toilet.title}</td>
                                    <td>[{toilet.location.x},{toilet.location.y}]</td>
                                    <td>{toilet.rating}</td>
                                </tr>
                            ))}
                            </tbody>
                        </table>
                    </CardBody>
                </Card>
            </GridItem>
        );
    }
}

export default ToiletList;
